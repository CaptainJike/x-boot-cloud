SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `learner_account`;
CREATE TABLE `learner_account` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `learner_no` varchar(64) NOT NULL COMMENT '学习者编号',
  `nickname` varchar(255) NOT NULL DEFAULT '' COMMENT '昵称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `phone_no` varchar(20) DEFAULT NULL COMMENT '手机号',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时刻',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `github_user_id` varchar(64) DEFAULT NULL COMMENT 'GitHub 用户ID',
  `github_login` varchar(255) DEFAULT NULL COMMENT 'GitHub 登录名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learner_account_no` (`learner_no`),
  UNIQUE KEY `uk_learner_account_email` (`email`),
  UNIQUE KEY `uk_learner_account_github_user` (`github_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习者账号';

DROP TABLE IF EXISTS `learner_profile`;
CREATE TABLE `learner_profile` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `preferred_learning_style` varchar(500) NOT NULL DEFAULT '' COMMENT '偏好学习风格',
  `latest_self_assessment` longtext NOT NULL COMMENT '最近自评基础',
  `focus_area` varchar(255) NOT NULL DEFAULT '' COMMENT '当前聚焦方向',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learner_profile_user` (`tenant_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习者画像';

DROP TABLE IF EXISTS `learning_goal`;
CREATE TABLE `learning_goal` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_topic` varchar(120) NOT NULL COMMENT '目标主题',
  `self_assessment` longtext NOT NULL COMMENT '自评基础',
  `weekly_learning_minutes` int NOT NULL COMMENT '每周学习分钟数',
  `preferred_learning_style` varchar(500) NOT NULL DEFAULT '' COMMENT '偏好学习风格',
  `template_code` varchar(64) NOT NULL COMMENT '模板编码',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `active_node_id` bigint DEFAULT NULL COMMENT '当前激活节点ID',
  `estimated_days` int DEFAULT NULL COMMENT '预计完成天数',
  PRIMARY KEY (`id`),
  KEY `idx_learning_goal_user_status` (`tenant_id`, `user_id`, `status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习目标';

DROP TABLE IF EXISTS `learning_map`;
CREATE TABLE `learning_map` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `generation_version` int NOT NULL DEFAULT '1' COMMENT '生成版本',
  `generation_summary` longtext NOT NULL COMMENT '生成摘要',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_map_goal` (`tenant_id`, `goal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习地图';

DROP TABLE IF EXISTS `learning_template_asset`;
CREATE TABLE `learning_template_asset` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `template_type` varchar(16) NOT NULL COMMENT '模板类型',
  `name` varchar(255) NOT NULL COMMENT '模板名称',
  `summary` longtext NOT NULL COMMENT '模板摘要',
  `domain` varchar(255) NOT NULL DEFAULT '' COMMENT '模板领域',
  `audience` varchar(500) NOT NULL DEFAULT '' COMMENT '适用人群',
  `tags_json` longtext COMMENT '标签 JSON',
  `visibility` varchar(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '可见性',
  `market_intent` tinyint NOT NULL DEFAULT '0' COMMENT '是否有市场化意图',
  `publish_status` varchar(32) NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态',
  `usage_count` int NOT NULL DEFAULT '0' COMMENT '使用次数',
  `source_type` varchar(32) NOT NULL DEFAULT 'manual' COMMENT '来源类型',
  `brief_json` longtext NOT NULL COMMENT 'Goal Brief JSON',
  `generation_summary` longtext COMMENT '地图生成摘要',
  `map_snapshot_json` longtext COMMENT '地图快照 JSON',
  PRIMARY KEY (`id`),
  KEY `idx_learning_template_asset_user_type` (`tenant_id`, `user_id`, `template_type`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习模板资产';

DROP TABLE IF EXISTS `learning_goal_context_record`;
CREATE TABLE `learning_goal_context_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `record_type` varchar(32) NOT NULL COMMENT '记录类型',
  `scope_key` varchar(128) NOT NULL COMMENT '作用域键',
  `goal_id` bigint DEFAULT NULL COMMENT '主目标ID',
  `related_goal_id` bigint DEFAULT NULL COMMENT '关联目标ID',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '记录标题',
  `payload_json` longtext NOT NULL COMMENT '上下文载荷 JSON',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_goal_context_scope` (`tenant_id`, `user_id`, `record_type`, `scope_key`),
  KEY `idx_learning_goal_context_type_time` (`tenant_id`, `user_id`, `record_type`, `updated_at`),
  KEY `idx_learning_goal_context_goal_time` (`tenant_id`, `user_id`, `goal_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='目标上下文记录';

DROP TABLE IF EXISTS `learning_map_node`;
CREATE TABLE `learning_map_node` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_id` bigint NOT NULL COMMENT '地图ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `node_code` varchar(64) NOT NULL COMMENT '节点编码',
  `title` varchar(120) NOT NULL COMMENT '节点标题',
  `description` longtext NOT NULL COMMENT '节点描述',
  `learning_objective` longtext NOT NULL COMMENT '学习目标',
  `why_it_matters` longtext NOT NULL COMMENT '为什么学',
  `estimated_minutes` int NOT NULL DEFAULT '30' COMMENT '建议学习分钟数',
  `difficulty_level` int NOT NULL DEFAULT '1' COMMENT '难度等级',
  `verification_method` varchar(500) NOT NULL DEFAULT '' COMMENT '验证方式',
  `completion_criteria` longtext NOT NULL COMMENT '完成条件',
  `prerequisite_node_codes` longtext NOT NULL COMMENT '前置节点编码',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_map_node_goal_code` (`tenant_id`, `goal_id`, `node_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习地图节点';

DROP TABLE IF EXISTS `learning_node_progress`;
CREATE TABLE `learning_node_progress` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_node_id` bigint NOT NULL COMMENT '节点ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '节点状态',
  `mastery_level` int NOT NULL DEFAULT '0' COMMENT '掌握等级',
  `last_diagnosis` varchar(32) DEFAULT NULL COMMENT '最近诊断结果',
  `last_studied_at` datetime DEFAULT NULL COMMENT '最近学习时刻',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时刻',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_node_progress_node` (`tenant_id`, `goal_id`, `map_node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='节点学习进度';

DROP TABLE IF EXISTS `tutor_session`;
CREATE TABLE `tutor_session` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_node_id` bigint NOT NULL COMMENT '节点ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态',
  `learner_question` longtext NOT NULL COMMENT '用户诉求',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tutor 会话';

DROP TABLE IF EXISTS `tutor_turn`;
CREATE TABLE `tutor_turn` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_node_id` bigint NOT NULL COMMENT '节点ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `turn_no` int NOT NULL COMMENT '轮次序号',
  `learner_answer` longtext NOT NULL COMMENT '用户回答',
  `diagnosis` varchar(32) NOT NULL DEFAULT 'needs_prereq' COMMENT '诊断结果',
  `action_type` varchar(32) NOT NULL DEFAULT 'diagnose' COMMENT 'Tutor 动作',
  `diagnostic_questions_json` longtext COMMENT '诊断问题 JSON',
  `tutor_response` longtext COMMENT 'Tutor 回复',
  `next_step_suggestions_json` longtext COMMENT '下一步建议 JSON',
  `recommended_node_id` bigint DEFAULT NULL COMMENT '推荐节点ID',
  `node_completed` tinyint NOT NULL DEFAULT '0' COMMENT '节点是否完成',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Tutor 轮次';

DROP TABLE IF EXISTS `reflection_entry`;
CREATE TABLE `reflection_entry` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint DEFAULT NULL COMMENT '目标ID',
  `reflection_date` date NOT NULL COMMENT '反思日期',
  `learned_today` longtext NOT NULL COMMENT '今天学到了什么',
  `biggest_insight` longtext NOT NULL COMMENT '今天最大的收获',
  `new_awareness` longtext NOT NULL COMMENT '今天新的认知',
  `unresolved_question` longtext NOT NULL COMMENT '今天哪里不会',
  `why_stuck` longtext NOT NULL COMMENT '为什么不会',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reflection_entry_user_date` (`tenant_id`, `user_id`, `reflection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日反思';

DROP TABLE IF EXISTS `practice_task`;
CREATE TABLE `practice_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_node_id` bigint NOT NULL COMMENT '地图节点ID',
  `task_key` varchar(128) NOT NULL COMMENT '跨端稳定任务键',
  `task_type` varchar(32) NOT NULL COMMENT '任务类型',
  `title` varchar(255) NOT NULL COMMENT '任务标题',
  `prompt` longtext NOT NULL COMMENT '任务提示',
  `expected_outcome` longtext NOT NULL COMMENT '预期输出',
  `hint` longtext NOT NULL COMMENT '任务提示',
  `estimated_minutes` int NOT NULL DEFAULT '10' COMMENT '预计分钟数',
  `node_title` varchar(255) NOT NULL DEFAULT '' COMMENT '节点标题快照',
  `source_diagnosis` varchar(32) DEFAULT NULL COMMENT '来源诊断',
  `related_concepts_json` longtext COMMENT '相关概念 JSON',
  `evidence_kind` varchar(32) NOT NULL COMMENT '证据类型',
  `knowledge_focus` longtext COMMENT '知识薄弱链路',
  `handoff_validation` tinyint NOT NULL DEFAULT '0' COMMENT '是否目标交接验证',
  `handoff_title` varchar(255) DEFAULT NULL COMMENT '交接标题',
  `task_version` int NOT NULL DEFAULT '1' COMMENT '任务版本',
  `active` tinyint NOT NULL DEFAULT '1' COMMENT '是否当前有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_practice_task_key` (`tenant_id`, `user_id`, `goal_id`, `task_key`),
  KEY `idx_practice_task_current` (`tenant_id`, `user_id`, `goal_id`, `map_node_id`, `active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习任务';

DROP TABLE IF EXISTS `practice_attempt`;
CREATE TABLE `practice_attempt` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `practice_task_id` bigint NOT NULL COMMENT '练习任务ID',
  `map_node_id` bigint NOT NULL COMMENT '地图节点ID',
  `attempt_key` varchar(128) NOT NULL COMMENT '尝试稳定键',
  `response_content` longtext NOT NULL COMMENT '练习回答',
  `self_rating` varchar(32) NOT NULL COMMENT '学习者自评',
  `artifacts_json` longtext COMMENT '证据列表 JSON',
  `assessment_json` longtext COMMENT '规则评测 JSON',
  `completed` tinyint NOT NULL DEFAULT '0' COMMENT '是否完成',
  `handoff_validation` tinyint NOT NULL DEFAULT '0' COMMENT '是否目标交接验证',
  `client_updated_at` varchar(40) DEFAULT NULL COMMENT '客户端更新时间',
  `last_mutation_id` varchar(64) NOT NULL COMMENT '最近客户端变更ID',
  `sync_version` bigint NOT NULL DEFAULT '1' COMMENT '跨设备同步版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_practice_attempt_task` (`tenant_id`, `user_id`, `practice_task_id`),
  KEY `idx_practice_attempt_goal` (`tenant_id`, `user_id`, `goal_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='练习提交记录';

DROP TABLE IF EXISTS `review_task`;
CREATE TABLE `review_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `map_node_id` bigint NOT NULL COMMENT '地图节点ID',
  `task_key` varchar(128) NOT NULL COMMENT '跨端稳定任务键',
  `task_type` varchar(32) NOT NULL COMMENT '任务类型',
  `title` varchar(255) NOT NULL COMMENT '任务标题',
  `prompt` longtext NOT NULL COMMENT '任务提示',
  `expected_outcome` longtext NOT NULL COMMENT '预期输出',
  `hint` longtext NOT NULL COMMENT '任务提示',
  `estimated_minutes` int NOT NULL DEFAULT '8' COMMENT '预计分钟数',
  `node_title` varchar(255) NOT NULL DEFAULT '' COMMENT '节点标题快照',
  `source_reason` longtext NOT NULL COMMENT '任务来源原因',
  `knowledge_focus` longtext COMMENT '知识薄弱点',
  `due_at` datetime NOT NULL COMMENT '建议到期时间',
  `interval_days` int NOT NULL DEFAULT '1' COMMENT '建议间隔天数',
  `priority` varchar(16) NOT NULL DEFAULT 'due' COMMENT '优先级',
  `priority_score` int NOT NULL DEFAULT '0' COMMENT '优先级得分',
  `schedule_reason` varchar(32) NOT NULL DEFAULT 'scheduled' COMMENT '调度原因',
  `mastery_score` int DEFAULT NULL COMMENT '掌握度快照',
  `last_reviewed_at` datetime DEFAULT NULL COMMENT '最近复盘时间',
  `task_version` int NOT NULL DEFAULT '1' COMMENT '任务版本',
  `active` tinyint NOT NULL DEFAULT '1' COMMENT '是否当前有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_task_key` (`tenant_id`, `user_id`, `goal_id`, `task_key`),
  KEY `idx_review_task_current` (`tenant_id`, `user_id`, `goal_id`, `map_node_id`, `active`, `due_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复盘任务';

DROP TABLE IF EXISTS `review_attempt`;
CREATE TABLE `review_attempt` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint NOT NULL COMMENT '目标ID',
  `review_task_id` bigint NOT NULL COMMENT '复盘任务ID',
  `map_node_id` bigint NOT NULL COMMENT '地图节点ID',
  `attempt_key` varchar(128) NOT NULL COMMENT '尝试稳定键',
  `response_content` longtext NOT NULL COMMENT '复盘回答',
  `self_rating` varchar(32) NOT NULL COMMENT '学习者自评',
  `scheduled_due_at` datetime DEFAULT NULL COMMENT '原定到期时间',
  `interval_days` int DEFAULT NULL COMMENT '建议间隔天数',
  `mastery_score_at_attempt` int DEFAULT NULL COMMENT '提交时掌握度',
  `completed` tinyint NOT NULL DEFAULT '0' COMMENT '是否完成',
  `client_updated_at` varchar(40) DEFAULT NULL COMMENT '客户端更新时间',
  `last_mutation_id` varchar(64) NOT NULL COMMENT '最近客户端变更ID',
  `sync_version` bigint NOT NULL DEFAULT '1' COMMENT '跨设备同步版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_attempt_task` (`tenant_id`, `user_id`, `review_task_id`),
  KEY `idx_review_attempt_goal` (`tenant_id`, `user_id`, `goal_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复盘提交记录';

DROP TABLE IF EXISTS `daily_digest`;
CREATE TABLE `daily_digest` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint DEFAULT NULL COMMENT '目标ID',
  `reflection_entry_id` bigint NOT NULL COMMENT '反思记录ID',
  `digest_date` date NOT NULL COMMENT '日报日期',
  `summary` longtext NOT NULL COMMENT '日报摘要',
  `next_action` longtext NOT NULL COMMENT '下一步行动',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_digest_user_date` (`tenant_id`, `user_id`, `digest_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成长日报';

DROP TABLE IF EXISTS `growth_snapshot`;
CREATE TABLE `growth_snapshot` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint DEFAULT NULL COMMENT '目标ID',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '标题',
  `summary` longtext NOT NULL COMMENT '摘要',
  PRIMARY KEY (`id`),
  KEY `idx_growth_snapshot_user_date` (`tenant_id`, `user_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成长快照';

DROP TABLE IF EXISTS `learning_event`;
CREATE TABLE `learning_event` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `revision` bigint NOT NULL DEFAULT '1' COMMENT '乐观锁',
  `del_flag` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除标识',
  `created_at` datetime NOT NULL COMMENT '创建时刻',
  `created_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime NOT NULL COMMENT '更新时刻',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goal_id` bigint DEFAULT NULL COMMENT '目标ID',
  `map_node_id` bigint DEFAULT NULL COMMENT '地图节点ID',
  `event_source` varchar(32) NOT NULL COMMENT '事件来源',
  `event_type` varchar(64) NOT NULL COMMENT '事件细分类型',
  `event_status` varchar(32) NOT NULL DEFAULT '' COMMENT '事件状态',
  `title` varchar(255) NOT NULL DEFAULT '' COMMENT '标题',
  `summary` longtext NOT NULL COMMENT '摘要',
  `related_entity_type` varchar(64) DEFAULT NULL COMMENT '关联实体类型',
  `related_entity_id` bigint DEFAULT NULL COMMENT '关联实体ID',
  `event_at` datetime NOT NULL COMMENT '事件发生时刻',
  `payload_json` longtext COMMENT '事件载荷 JSON',
  PRIMARY KEY (`id`),
  KEY `idx_learning_event_user_time` (`tenant_id`, `user_id`, `event_at`),
  KEY `idx_learning_event_goal_time` (`tenant_id`, `user_id`, `goal_id`, `event_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一学习事件';

SET FOREIGN_KEY_CHECKS = 1;
