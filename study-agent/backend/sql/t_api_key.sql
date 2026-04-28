-- API Key 表
CREATE TABLE IF NOT EXISTS `t_api_key` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `provider` VARCHAR(50) NOT NULL default '' COMMENT '提供商名称，如 minimax, baidu-ocr',
  `api_key` VARCHAR(500) NOT NULL default '' COMMENT 'API密钥',
  `baidu_ocr_secret_key` VARCHAR(500) NOT NULL default '' COMMENT '百度OCR Secret密钥（仅用于百度OCR）',
  `base_url` VARCHAR(255) not null DEFAULT '' COMMENT 'API基础URL',
  `model_name` VARCHAR(255) not null DEFAULT '' COMMENT '模型名称',
  `enabled` TINYINT(1) not null DEFAULT TRUE COMMENT '是否启用',
  `del` TINYINT(1) not null DEFAULT 0 COMMENT '逻辑删除标记',
  `create_time` DATETIME not null DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` DATETIME not null DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider` (`provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';

-- 为已存在的表添加 baidu_ocr_secret_key 字段（迁移用）
ALTER TABLE `t_api_key` ADD COLUMN IF NOT EXISTS `baidu_ocr_secret_key` VARCHAR(500) NOT NULL default '' COMMENT '百度OCR Secret密钥（仅用于百度OCR）' AFTER `api_key`;

-- 初始化 MiniMax API Key（请替换为实际密钥）
INSERT INTO `t_api_key` (`provider`, `api_key`, `baidu_ocr_secret_key`, `base_url`, `model_name`, `enabled`)
VALUES ('minimax', 'your-api-key-here', '', 'https://api.minimaxi.com/anthropic/v1', 'MiniMax-M2.7', TRUE)
ON DUPLICATE KEY UPDATE `api_key` = VALUES(`api_key`), `base_url` = VALUES(`base_url`), `model_name` = VALUES(`model_name`);

-- 初始化百度 OCR 配置（请替换为实际密钥）
-- 注意：百度 OCR 的 URL（token-url 和 ocr-url）仍从配置文件 application.yml 读取
-- 仅 api_key 和 baidu_ocr_secret_key 从数据库读取
-- provider: baidu-ocr
-- api_key: 百度OCR的 API Key
-- baidu_ocr_secret_key: 百度OCR的 Secret Key
INSERT INTO `t_api_key` (`provider`, `api_key`, `baidu_ocr_secret_key`, `base_url`, `model_name`, `enabled`)
VALUES (
  'baidu-ocr',
  'your-baidu-api-key-here',
  'your-baidu-secret-key-here',
  '',
  '',
  TRUE
)
ON DUPLICATE KEY UPDATE 
  `api_key` = VALUES(`api_key`),
  `baidu_ocr_secret_key` = VALUES(`baidu_ocr_secret_key`);
