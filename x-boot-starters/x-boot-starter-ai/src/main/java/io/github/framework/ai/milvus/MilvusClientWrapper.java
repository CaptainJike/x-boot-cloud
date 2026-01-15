package io.github.framework.ai.milvus;

import io.github.framework.ai.properties.AiProperties;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.FlushParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Milvus client wrapper based on milvus-sdk-java v2.6.12
 *
 * Notes:
 * - This wrapper implements basic createCollection/upsert/search semantics.
 * - Production should add connection pooling, retry/backoff, metrics and more robust error handling.
 */
public class MilvusClientWrapper {

    private final MilvusServiceClient client;
    private final String collectionPrefix;
    private final int dimension;

    public MilvusClientWrapper(AiProperties.Milvus cfg) {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(cfg.getHost())
                .withPort(cfg.getPort())
                .build();
        this.client = new MilvusServiceClient(connectParam);
        this.collectionPrefix = cfg.getCollectionPrefix();
        this.dimension = cfg.getDimension();
    }

    public String getCollectionName(String name) {
        return collectionPrefix + name;
    }

    public boolean hasCollection(String collection) {
        R<Boolean> r = client.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collection)
                .build());
        if (r.getStatus().ok()) {
            return r.getData();
        }
        return false;
    }

    public void createCollectionIfNotExists(String collection, int dim) {
        String collectionName = collection;
        if (hasCollection(collectionName)) return;

        FieldType pk = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();

        FieldType vec = FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dim)
                .build();

        FieldType meta = FieldType.newBuilder()
                .withName("meta")
                .withDataType(DataType.VarChar)
                .withMaxLength(2048)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("x-boot-ai collection for " + collectionName)
                .withShardsNum(2)
                .addFieldType(pk)
                .addFieldType(vec)
                .addFieldType(meta)
                .build();

        R<RpcStatus> resp = client.createCollection(createCollectionReq);
        if (!resp.getStatus().ok()) {
            throw new RuntimeException("createCollection failed: " + resp.getStatus().toString());
        }

        // create default index (IVF_FLAT + IP/EF)
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                .withExtraParam("{\"nlist\":1024}")
                .withSyncMode(Boolean.FALSE)
                .build();
        client.createIndex(indexParam);
    }

    /**
     * Upsert vectors into Milvus.
     *
     * @param collection plain collection name (must be created via createCollectionIfNotExists)
     * @param ids        aligned ids (Long)
     * @param vectors    aligned float[] vectors
     * @param metas      aligned metadata strings (json)
     */
    public void upsert(String collection, List<Long> ids, List<float[]> vectors, List<String> metas) {
        String full = getCollectionName(collection);
        createCollectionIfNotExists(full, dimension);

        // Build fields
        List<InsertParam.Field> fields = new ArrayList<>();
        // id field
        fields.add(new InsertParam.Field("id", ids));
        // embedding list -> Milvus SDK expects List<List<Float>>
        List<List<Float>> floatVectors = vectors.stream()
                .map(vec -> {
                    List<Float> list = new ArrayList<>(vec.length);
                    for (float v : vec) list.add(v);
                    return list;
                }).collect(Collectors.toList());
        fields.add(new InsertParam.Field("embedding", floatVectors));
        fields.add(new InsertParam.Field("meta", metas));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(full)
                .withFields(fields)
                .build();

        R<MutationResult> res = client.insert(insertParam);
        if (!res.getStatus().ok()) {
            throw new RuntimeException("Milvus insert failed: " + res.getStatus().toString());
        }

        // flush to make vectors searchable
        client.flush(FlushParam.newBuilder().withCollectionNames(Collections.singletonList(full)).build());
    }

    /**
     * Search vectors in collection. Returns list of hits: (id, score, meta).
     *
     * @param collection plain name
     * @param queryVec   query vector
     * @param topK
     * @return list of SearchHit
     */
    public List<SearchHit> search(String collection, float[] queryVec, int topK) {
        String full = getCollectionName(collection);
        if (!hasCollection(full)) return Collections.emptyList();

        // convert queryVec to List<Float>
        List<Float> qv = new ArrayList<>(queryVec.length);
        for (float v : queryVec) qv.add(v);
        List<List<Float>> vectorsToSearch = Collections.singletonList(qv);

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(full)
                .withMetricType(MetricType.IP)
                .withOutFields(Arrays.asList("meta"))
                .withTopK(topK)
                .withVectors(vectorsToSearch)
                .withVectorFieldName("embedding")
                .withParams("{\"nprobe\":16}")
                .build();

        R<SearchResults> r = client.search(searchParam);
        if (!r.getStatus().ok()) {
            throw new RuntimeException("Milvus search failed: " + r.getStatus().toString());
        }

        SearchResults results = r.getData();
        List<SearchHit> hits = new ArrayList<>();
        // parse results: iterate topK for first query
        SearchResultsWrapper wrapper = new SearchResultsWrapper(results.getResults());
        int top = wrapper.getRowNum();
        for (int i = 0; i < top && i < topK; i++) {
            long id = wrapper.getFieldAsLong(i, 0); // primary key idx
            double score = wrapper.getScore(i, 0);
            String meta = wrapper.getFieldAsString(i, "meta");
            hits.add(new SearchHit(id, score, meta));
        }
        return hits;
    }

    public static class SearchHit {
        public final long id;
        public final double score;
        public final String meta;

        public SearchHit(long id, double score, String meta) {
            this.id = id;
            this.score = score;
            this.meta = meta;
        }
    }
}
