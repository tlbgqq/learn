-- API Key 表
CREATE TABLE IF NOT EXISTS `t_api_key` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `provider` VARCHAR(50) NOT NULL default '' COMMENT '提供商名称，如 minimax',
  `api_key` VARCHAR(500) NOT NULL default '' COMMENT 'API密钥',
  `base_url` VARCHAR(255) not null DEFAULT '' COMMENT 'API基础URL',
  `model_name` VARCHAR(100) not null DEFAULT '' COMMENT '模型名称',
  `enabled` TINYINT(1) not null DEFAULT TRUE COMMENT '是否启用',
  `del` TINYINT(1) not null DEFAULT 0 COMMENT '逻辑删除标记',
  `create_time` DATETIME not null DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` DATETIME not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider` (`provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';

-- 初始化 MiniMax API Key（请替换为实际密钥）
INSERT INTO `t_api_key` (`provider`, `api_key`, `base_url`, `model_name`, `enabled`)
VALUES ('minimax', 'your-api-key-here', 'https://api.minimaxi.com/anthropic/v1', 'MiniMax-M2.7', TRUE)
ON DUPLICATE KEY UPDATE `api_key` = VALUES(`api_key`);
