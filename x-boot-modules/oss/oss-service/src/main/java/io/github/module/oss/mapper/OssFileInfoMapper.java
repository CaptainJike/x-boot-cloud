package io.github.module.oss.mapper;

import io.github.module.oss.entity.OssFileInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * 上传文件信息
 */
@Mapper
public interface OssFileInfoMapper extends BaseMapper<OssFileInfoEntity> {

}
