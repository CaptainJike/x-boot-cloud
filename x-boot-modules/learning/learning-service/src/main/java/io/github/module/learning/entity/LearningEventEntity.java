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

import java.time.LocalDateTime;

/**
 * 统一学习事件.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learning_event")
public class LearningEventEntity extends BaseEntity<Long> {

    @TableField("user_id")
    private Long userId;

    @TableField("goal_id")
    private Long goalId;

    @TableField("map_node_id")
    private Long mapNodeId;

    @TableField("event_source")
    private String eventSource;

    @TableField("event_type")
    private String eventType;

    @TableField("event_status")
    private String eventStatus;

    @TableField("title")
    private String title;

    @TableField("summary")
    private String summary;

    @TableField("related_entity_type")
    private String relatedEntityType;

    @TableField("related_entity_id")
    private Long relatedEntityId;

    @TableField("event_at")
    private LocalDateTime eventAt;

    @TableField("payload_json")
    private String payloadJson;
}
