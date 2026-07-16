package io.github.module.learning.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.crud.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 学习模板资产.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_template_asset")
public class LearningTemplateAssetEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("template_type")
    private String templateType;

    @TableField("name")
    private String name;

    @TableField("summary")
    private String summary;

    @TableField("domain")
    private String domain;

    @TableField("audience")
    private String audience;

    @TableField("tags_json")
    private String tagsJson;

    @TableField("visibility")
    private String visibility;

    @TableField("market_intent")
    private Integer marketIntent;

    @TableField("publish_status")
    private String publishStatus;

    @TableField("usage_count")
    private Integer usageCount;

    @TableField("source_type")
    private String sourceType;

    @TableField("brief_json")
    private String briefJson;

    @TableField("generation_summary")
    private String generationSummary;

    @TableField("map_snapshot_json")
    private String mapSnapshotJson;
}
