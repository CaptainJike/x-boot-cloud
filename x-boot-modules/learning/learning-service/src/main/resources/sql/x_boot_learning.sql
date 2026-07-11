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
  `email` varchar(255) NOT NULL DEFAULT '' COMMENT '邮箱',
  `phone_no` varchar(20) DEFAULT NULL COMMENT '手机号',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时刻',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `github_user_id` varchar(64) NOT NULL COMMENT 'GitHub 用户ID',
  `github_login` varchar(255) NOT NULL COMMENT 'GitHub 登录名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learner_account_no` (`learner_no`),
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
  `latest_self_assessment` varchar(2000) NOT NULL DEFAULT '' COMMENT '最近自评基础',
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
  `self_assessment` varchar(2000) NOT NULL DEFAULT '' COMMENT '自评基础',
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
  `generation_summary` varchar(1000) NOT NULL DEFAULT '' COMMENT '生成摘要',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_map_goal` (`tenant_id`, `goal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习地图';

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
  `description` varchar(1000) NOT NULL DEFAULT '' COMMENT '节点描述',
  `learning_objective` varchar(1000) NOT NULL DEFAULT '' COMMENT '学习目标',
  `why_it_matters` varchar(1000) NOT NULL DEFAULT '' COMMENT '为什么学',
  `estimated_minutes` int NOT NULL DEFAULT '30' COMMENT '建议学习分钟数',
  `difficulty_level` int NOT NULL DEFAULT '1' COMMENT '难度等级',
  `verification_method` varchar(500) NOT NULL DEFAULT '' COMMENT '验证方式',
  `completion_criteria` varchar(1000) NOT NULL DEFAULT '' COMMENT '完成条件',
  `prerequisite_node_codes` varchar(1000) NOT NULL DEFAULT '' COMMENT '前置节点编码',
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
  `learner_question` varchar(4000) NOT NULL DEFAULT '' COMMENT '用户诉求',
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
  `learner_answer` varchar(4000) NOT NULL DEFAULT '' COMMENT '用户回答',
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
  `summary` varchar(4000) NOT NULL DEFAULT '' COMMENT '日报摘要',
  `next_action` varchar(1000) NOT NULL DEFAULT '' COMMENT '下一步行动',
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
  `summary` longtext NOT NULL DEFAULT '' COMMENT '摘要',
  PRIMARY KEY (`id`),
  KEY `idx_growth_snapshot_user_date` (`tenant_id`, `user_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成长快照';

SET FOREIGN_KEY_CHECKS = 1;
