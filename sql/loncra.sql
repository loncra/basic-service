/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : localhost:3306
 Source Schema         : loncra

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 14/07/2026 11:33:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_batch_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_batch_message`;
CREATE TABLE `tb_batch_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `execute_status` tinyint NOT NULL COMMENT '状态:0.执行中、1.执行成功，99.执行失败',
  `count` smallint NOT NULL COMMENT '总数',
  `success_number` smallint NULL DEFAULT NULL COMMENT '成功发送数量',
  `fail_number` smallint NULL DEFAULT NULL COMMENT '失败发送数量',
  `type` smallint NOT NULL COMMENT '类型:10.站内信,20.邮件,30.短信',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '批量消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_batch_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_carousel
-- ----------------------------
DROP TABLE IF EXISTS `tb_carousel`;
CREATE TABLE `tb_carousel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `type` tinyint NOT NULL COMMENT '类型',
  `link` json NOT NULL COMMENT '链接',
  `status` tinyint NOT NULL COMMENT '状态',
  `cover` json NULL COMMENT '封面',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` smallint NOT NULL DEFAULT 0 COMMENT '顺序值',
  `expiration_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `showtime` datetime NULL DEFAULT NULL COMMENT '展示时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_carousel
-- ----------------------------

-- ----------------------------
-- Table structure for tb_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user`;
CREATE TABLE `tb_console_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `password` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '登录帐号',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别:10.男,20.女',
  `real_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '真实姓名',
  `real_name_authentication` tinyint NULL DEFAULT 0 COMMENT '是否实名认证:1.是，0.否',
  `phone_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '电话号码',
  `last_authentication_time` datetime NULL DEFAULT NULL COMMENT '最后认证(登入)时间',
  `phone_number_verified` tinyint NULL DEFAULT 0 COMMENT '是验证码手机号码',
  `email_verified` tinyint NULL DEFAULT 0 COMMENT '是否验证邮箱',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `metadata` json NULL COMMENT '元数据信息',
  `initialization` json NULL COMMENT '初始化元数据信息',
  `role_ids` json NULL COMMENT '组信息',
  `resource_ids` json NULL COMMENT '资源信息',
  `avatar` json NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `ux_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `ux_phone_number`(`phone_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '后台用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_console_user
-- ----------------------------
INSERT INTO `tb_console_user` VALUES (1, '2021-08-18 09:40:46.953', 1, 'WkdTZQXTbCu4LfuuNAmhE/6HagxSLfZzEnuCX+nn7YM=', '$2a$10$boM5YDZtGH.m1FLhwGmPUOsJHQFXIDGx1TaBjMf7z/6md2DD35cJK', 1, 'admin', 10, '超级管理员', 0, 'SFwjCWNb58W9IWle9MsZuQ==', '2026-07-14 08:17:41', 1, 1, NULL, NULL, NULL, '[1]', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 43, 46, 47, 48, 49, 50, 51, 56, 61, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 76, 78, 79, 81, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 103, 106, 107, 108, 109, 111, 114, 115, 117, 119]', '{\"id\": \"ebbca097e1bede8a54a3af4c151efe9f\", \"etag\": \"ce1d4bbcf5878a24829d6842ad1585dd\", \"size\": 261779, \"bucketName\": \"loncra.basic-service.resource.avatar\", \"objectName\": \"CONSOLE:1/f6f86f1ea4a41d6e18b1887500523271.jpg\", \"extraHeaders\": {\"content-type\": \"image/jpeg\", \"X-Amz-Meta-Tenant-Id\": \"CONSOLE:1\", \"X-Amz-Meta-Uploader-Id\": \"CONSOLE:1\", \"X-Amz-Meta-Original-Filename\": \"微信图片_20250215091805_32.jpg\"}, \"lastModified\": 1781484372905000}');
INSERT INTO `tb_console_user` VALUES (105, '2026-05-28 08:11:43.332', 1, '3Fzt7m0YNp7ynJS4UMxVqL5X/mKFFriB672NSoLbN3w=', '$2a$10$7cyNZ1b0GVMO2j22xetNAeIyN5jUlofhkWXcrKqT7fVTNwrIOGFu6', 1, '18776975533', 30, '5533', 0, 'za30fiPRYSFmZi8EjJWrrw==', '2026-07-14 08:19:27', 0, 0, '', NULL, '{\"randomPassword\": {\"name\": \"是\", \"value\": 1}, \"randomUsername\": {\"name\": \"是\", \"value\": 1}}', '[1]', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 43, 46, 47, 48, 49, 50, 51, 56, 61, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 76, 78, 79, 81, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 103, 106, 107, 108, 109, 111, 114, 115, 117, 119]', '{\"id\": \"7fe7974d1697ffbc6708d5075659f5b4\", \"etag\": \"\\\"d8aa2b9c72646ca8ce68cb7ebcb468c0-2\\\"\", \"size\": 5515705, \"bucketName\": \"loncra.basic-service.resource.avatar\", \"objectName\": \"CONSOLE:105/0dfac2a4a74fc6c7bfe0371858366105.JPG\", \"extraHeaders\": {\"Content-Type\": \"image/jpeg\", \"x-amz-meta-tenant-id\": \"CONSOLE:105\", \"x-amz-meta-uploader-id\": \"CONSOLE:105\", \"x-amz-meta-original-filename\": \"IMG_0153.JPG\"}}');
INSERT INTO `tb_console_user` VALUES (106, '2026-05-28 08:12:33.838', 1, 'SAlc4VmzUsNSB+H08SiFir5X/mKFFriB672NSoLbN3w=', '$2a$10$7cyNZ1b0GVMO2j22xetNAeIyN5jUlofhkWXcrKqT7fVTNwrIOGFu6', 1, '18776975544', 30, '5544', 0, 'ocDuXv1LOKdDIA21N1gGtw==', '2026-06-28 15:37:24', 0, 0, '', NULL, '{\"randomPassword\": {\"name\": \"是\", \"value\": 1}, \"randomUsername\": {\"name\": \"是\", \"value\": 1}}', '[1]', '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 43, 46, 47, 48, 49, 50, 51, 56, 61, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 76, 78, 79, 81, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 103, 106, 107, 108, 109, 111, 114, 115, 117, 119]', NULL);

-- ----------------------------
-- Table structure for tb_data_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `tb_data_dictionary`;
CREATE TABLE `tb_data_dictionary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `code` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '键名称',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '等级',
  `value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '值',
  `value_type` smallint NULL DEFAULT NULL COMMENT '值类型',
  `metadata` json NULL,
  `enabled` tinyint NULL DEFAULT 1 COMMENT '状态:0.禁用,1.启用',
  `type_id` bigint NOT NULL COMMENT '对应字典类型',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '根节点为 null',
  `sort` smallint NULL DEFAULT NULL COMMENT '顺序值',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_code`(`code` ASC) USING BTREE,
  INDEX `ix_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `ix_type_id`(`type_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3654 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_data_dictionary
-- ----------------------------
INSERT INTO `tb_data_dictionary` VALUES (40, '2020-03-29 14:20:36.000', 1, 'system.region.province.110000', '北京市', 'area', '110000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (41, '2020-03-29 14:20:36.000', 1, 'system.region.area.110101', '东城区', NULL, '110101', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (42, '2020-03-29 14:20:36.000', 1, 'system.region.area.110102', '西城区', NULL, '110102', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (43, '2020-03-29 14:20:36.000', 1, 'system.region.area.110105', '朝阳区', NULL, '110105', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (44, '2020-03-29 14:20:36.000', 1, 'system.region.area.110106', '丰台区', NULL, '110106', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (45, '2020-03-29 14:20:36.000', 1, 'system.region.area.110107', '石景山区', NULL, '110107', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (46, '2020-03-29 14:20:36.000', 1, 'system.region.area.110108', '海淀区', NULL, '110108', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (47, '2020-03-29 14:20:36.000', 1, 'system.region.area.110109', '门头沟区', NULL, '110109', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (48, '2020-03-29 14:20:36.000', 1, 'system.region.area.110111', '房山区', NULL, '110111', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (49, '2020-03-29 14:20:36.000', 1, 'system.region.area.110112', '通州区', NULL, '110112', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (50, '2020-03-29 14:20:36.000', 1, 'system.region.area.110113', '顺义区', NULL, '110113', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (51, '2020-03-29 14:20:36.000', 1, 'system.region.area.110114', '昌平区', NULL, '110114', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (52, '2020-03-29 14:20:36.000', 1, 'system.region.area.110115', '大兴区', NULL, '110115', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (53, '2020-03-29 14:20:36.000', 1, 'system.region.area.110116', '怀柔区', NULL, '110116', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (54, '2020-03-29 14:20:36.000', 1, 'system.region.area.110117', '平谷区', NULL, '110117', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (55, '2020-03-29 14:20:36.000', 1, 'system.region.area.110118', '密云区', NULL, '110118', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (56, '2020-03-29 14:20:36.000', 1, 'system.region.area.110119', '延庆区', NULL, '110119', 30, NULL, 1, 16, 40, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (57, '2020-03-29 14:20:36.000', 1, 'system.region.province.120000', '天津市', 'area', '120000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (58, '2020-03-29 14:20:36.000', 1, 'system.region.area.120101', '和平区', NULL, '120101', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (59, '2020-03-29 14:20:36.000', 1, 'system.region.area.120102', '河东区', NULL, '120102', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (60, '2020-03-29 14:20:36.000', 1, 'system.region.area.120103', '河西区', NULL, '120103', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (61, '2020-03-29 14:20:36.000', 1, 'system.region.area.120104', '南开区', NULL, '120104', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (62, '2020-03-29 14:20:36.000', 1, 'system.region.area.120105', '河北区', NULL, '120105', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (63, '2020-03-29 14:20:36.000', 1, 'system.region.area.120106', '红桥区', NULL, '120106', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (64, '2020-03-29 14:20:36.000', 1, 'system.region.area.120110', '东丽区', NULL, '120110', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (65, '2020-03-29 14:20:36.000', 1, 'system.region.area.120111', '西青区', NULL, '120111', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (66, '2020-03-29 14:20:36.000', 1, 'system.region.area.120112', '津南区', NULL, '120112', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (67, '2020-03-29 14:20:36.000', 1, 'system.region.area.120113', '北辰区', NULL, '120113', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (68, '2020-03-29 14:20:36.000', 1, 'system.region.area.120114', '武清区', NULL, '120114', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (69, '2020-03-29 14:20:36.000', 1, 'system.region.area.120115', '宝坻区', NULL, '120115', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (70, '2020-03-29 14:20:36.000', 1, 'system.region.area.120116', '滨海新区', NULL, '120116', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (71, '2020-03-29 14:20:36.000', 1, 'system.region.area.120117', '宁河区', NULL, '120117', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (72, '2020-03-29 14:20:36.000', 1, 'system.region.area.120118', '静海区', NULL, '120118', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (73, '2020-03-29 14:20:36.000', 1, 'system.region.area.120119', '蓟州区', NULL, '120119', 30, NULL, 1, 16, 57, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (74, '2020-03-29 14:20:36.000', 1, 'system.region.province.130000', '河北省', 'city', '130000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (75, '2020-03-29 14:20:36.000', 1, 'system.region.city.130100', '石家庄市', 'area', '130100', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (76, '2020-03-29 14:20:36.000', 1, 'system.region.area.130102', '长安区', NULL, '130102', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (77, '2020-03-29 14:20:36.000', 1, 'system.region.area.130104', '桥西区', NULL, '130104', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (78, '2020-03-29 14:20:36.000', 1, 'system.region.area.130105', '新华区', NULL, '130105', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (79, '2020-03-29 14:20:36.000', 1, 'system.region.area.130107', '井陉矿区', NULL, '130107', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (80, '2020-03-29 14:20:36.000', 1, 'system.region.area.130108', '裕华区', NULL, '130108', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (81, '2020-03-29 14:20:36.000', 1, 'system.region.area.130109', '藁城区', NULL, '130109', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (82, '2020-03-29 14:20:36.000', 1, 'system.region.area.130110', '鹿泉区', NULL, '130110', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (83, '2020-03-29 14:20:36.000', 1, 'system.region.area.130111', '栾城区', NULL, '130111', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (84, '2020-03-29 14:20:36.000', 1, 'system.region.area.130121', '井陉县', NULL, '130121', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (85, '2020-03-29 14:20:36.000', 1, 'system.region.area.130123', '正定县', NULL, '130123', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (86, '2020-03-29 14:20:36.000', 1, 'system.region.area.130125', '行唐县', NULL, '130125', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (87, '2020-03-29 14:20:36.000', 1, 'system.region.area.130126', '灵寿县', NULL, '130126', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (88, '2020-03-29 14:20:36.000', 1, 'system.region.area.130127', '高邑县', NULL, '130127', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (89, '2020-03-29 14:20:36.000', 1, 'system.region.area.130128', '深泽县', NULL, '130128', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (90, '2020-03-29 14:20:36.000', 1, 'system.region.area.130129', '赞皇县', NULL, '130129', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (91, '2020-03-29 14:20:36.000', 1, 'system.region.area.130130', '无极县', NULL, '130130', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (92, '2020-03-29 14:20:36.000', 1, 'system.region.area.130131', '平山县', NULL, '130131', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (93, '2020-03-29 14:20:36.000', 1, 'system.region.area.130132', '元氏县', NULL, '130132', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (94, '2020-03-29 14:20:36.000', 1, 'system.region.area.130133', '赵县', NULL, '130133', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (95, '2020-03-29 14:20:36.000', 1, 'system.region.area.130181', '辛集市', NULL, '130181', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (96, '2020-03-29 14:20:36.000', 1, 'system.region.area.130183', '晋州市', NULL, '130183', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (97, '2020-03-29 14:20:36.000', 1, 'system.region.area.130184', '新乐市', NULL, '130184', 30, NULL, 1, 16, 75, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (98, '2020-03-29 14:20:36.000', 1, 'system.region.city.130200', '唐山市', 'area', '130200', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (99, '2020-03-29 14:20:36.000', 1, 'system.region.area.130202', '路南区', NULL, '130202', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (100, '2020-03-29 14:20:36.000', 1, 'system.region.area.130203', '路北区', NULL, '130203', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (101, '2020-03-29 14:20:36.000', 1, 'system.region.area.130204', '古冶区', NULL, '130204', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (102, '2020-03-29 14:20:36.000', 1, 'system.region.area.130205', '开平区', NULL, '130205', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (103, '2020-03-29 14:20:36.000', 1, 'system.region.area.130207', '丰南区', NULL, '130207', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (104, '2020-03-29 14:20:36.000', 1, 'system.region.area.130208', '丰润区', NULL, '130208', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (105, '2020-03-29 14:20:36.000', 1, 'system.region.area.130209', '曹妃甸区', NULL, '130209', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (106, '2020-03-29 14:20:36.000', 1, 'system.region.area.130224', '滦南县', NULL, '130224', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (107, '2020-03-29 14:20:36.000', 1, 'system.region.area.130225', '乐亭县', NULL, '130225', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (108, '2020-03-29 14:20:36.000', 1, 'system.region.area.130227', '迁西县', NULL, '130227', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (109, '2020-03-29 14:20:36.000', 1, 'system.region.area.130229', '玉田县', NULL, '130229', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (110, '2020-03-29 14:20:36.000', 1, 'system.region.area.130281', '遵化市', NULL, '130281', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (111, '2020-03-29 14:20:36.000', 1, 'system.region.area.130283', '迁安市', NULL, '130283', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (112, '2020-03-29 14:20:36.000', 1, 'system.region.area.130284', '滦州市', NULL, '130284', 30, NULL, 1, 16, 98, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (113, '2020-03-29 14:20:36.000', 1, 'system.region.city.130300', '秦皇岛市', 'area', '130300', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (114, '2020-03-29 14:20:36.000', 1, 'system.region.area.130302', '海港区', NULL, '130302', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (115, '2020-03-29 14:20:36.000', 1, 'system.region.area.130303', '山海关区', NULL, '130303', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (116, '2020-03-29 14:20:36.000', 1, 'system.region.area.130304', '北戴河区', NULL, '130304', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (117, '2020-03-29 14:20:36.000', 1, 'system.region.area.130306', '抚宁区', NULL, '130306', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (118, '2020-03-29 14:20:36.000', 1, 'system.region.area.130321', '青龙满族自治县', NULL, '130321', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (119, '2020-03-29 14:20:36.000', 1, 'system.region.area.130322', '昌黎县', NULL, '130322', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (120, '2020-03-29 14:20:36.000', 1, 'system.region.area.130324', '卢龙县', NULL, '130324', 30, NULL, 1, 16, 113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (121, '2020-03-29 14:20:36.000', 1, 'system.region.city.130400', '邯郸市', 'area', '130400', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (122, '2020-03-29 14:20:36.000', 1, 'system.region.area.130402', '邯山区', NULL, '130402', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (123, '2020-03-29 14:20:36.000', 1, 'system.region.area.130403', '丛台区', NULL, '130403', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (124, '2020-03-29 14:20:36.000', 1, 'system.region.area.130404', '复兴区', NULL, '130404', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (125, '2020-03-29 14:20:36.000', 1, 'system.region.area.130406', '峰峰矿区', NULL, '130406', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (126, '2020-03-29 14:20:36.000', 1, 'system.region.area.130407', '肥乡区', NULL, '130407', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (127, '2020-03-29 14:20:36.000', 1, 'system.region.area.130408', '永年区', NULL, '130408', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (128, '2020-03-29 14:20:36.000', 1, 'system.region.area.130423', '临漳县', NULL, '130423', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (129, '2020-03-29 14:20:36.000', 1, 'system.region.area.130424', '成安县', NULL, '130424', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (130, '2020-03-29 14:20:36.000', 1, 'system.region.area.130425', '大名县', NULL, '130425', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (131, '2020-03-29 14:20:36.000', 1, 'system.region.area.130426', '涉县', NULL, '130426', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (132, '2020-03-29 14:20:36.000', 1, 'system.region.area.130427', '磁县', NULL, '130427', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (133, '2020-03-29 14:20:36.000', 1, 'system.region.area.130430', '邱县', NULL, '130430', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (134, '2020-03-29 14:20:36.000', 1, 'system.region.area.130431', '鸡泽县', NULL, '130431', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (135, '2020-03-29 14:20:36.000', 1, 'system.region.area.130432', '广平县', NULL, '130432', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (136, '2020-03-29 14:20:36.000', 1, 'system.region.area.130433', '馆陶县', NULL, '130433', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (137, '2020-03-29 14:20:36.000', 1, 'system.region.area.130434', '魏县', NULL, '130434', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (138, '2020-03-29 14:20:36.000', 1, 'system.region.area.130435', '曲周县', NULL, '130435', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (139, '2020-03-29 14:20:36.000', 1, 'system.region.area.130481', '武安市', NULL, '130481', 30, NULL, 1, 16, 121, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (140, '2020-03-29 14:20:36.000', 1, 'system.region.city.130500', '邢台市', 'area', '130500', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (141, '2020-03-29 14:20:36.000', 1, 'system.region.area.130502', '桥东区', NULL, '130502', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (142, '2020-03-29 14:20:36.000', 1, 'system.region.area.130503', '桥西区', NULL, '130503', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (143, '2020-03-29 14:20:36.000', 1, 'system.region.area.130521', '邢台县', NULL, '130521', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (144, '2020-03-29 14:20:36.000', 1, 'system.region.area.130522', '临城县', NULL, '130522', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (145, '2020-03-29 14:20:36.000', 1, 'system.region.area.130523', '内丘县', NULL, '130523', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (146, '2020-03-29 14:20:36.000', 1, 'system.region.area.130524', '柏乡县', NULL, '130524', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (147, '2020-03-29 14:20:36.000', 1, 'system.region.area.130525', '隆尧县', NULL, '130525', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (148, '2020-03-29 14:20:36.000', 1, 'system.region.area.130526', '任县', NULL, '130526', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (149, '2020-03-29 14:20:36.000', 1, 'system.region.area.130527', '南和县', NULL, '130527', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (150, '2020-03-29 14:20:36.000', 1, 'system.region.area.130528', '宁晋县', NULL, '130528', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (151, '2020-03-29 14:20:36.000', 1, 'system.region.area.130529', '巨鹿县', NULL, '130529', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (152, '2020-03-29 14:20:36.000', 1, 'system.region.area.130530', '新河县', NULL, '130530', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (153, '2020-03-29 14:20:36.000', 1, 'system.region.area.130531', '广宗县', NULL, '130531', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (154, '2020-03-29 14:20:36.000', 1, 'system.region.area.130532', '平乡县', NULL, '130532', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (155, '2020-03-29 14:20:36.000', 1, 'system.region.area.130533', '威县', NULL, '130533', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (156, '2020-03-29 14:20:36.000', 1, 'system.region.area.130534', '清河县', NULL, '130534', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (157, '2020-03-29 14:20:36.000', 1, 'system.region.area.130535', '临西县', NULL, '130535', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (158, '2020-03-29 14:20:36.000', 1, 'system.region.area.130581', '南宫市', NULL, '130581', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (159, '2020-03-29 14:20:36.000', 1, 'system.region.area.130582', '沙河市', NULL, '130582', 30, NULL, 1, 16, 140, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (160, '2020-03-29 14:20:36.000', 1, 'system.region.city.130600', '保定市', 'area', '130600', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (161, '2020-03-29 14:20:36.000', 1, 'system.region.area.130602', '竞秀区', NULL, '130602', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (162, '2020-03-29 14:20:36.000', 1, 'system.region.area.130606', '莲池区', NULL, '130606', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (163, '2020-03-29 14:20:36.000', 1, 'system.region.area.130607', '满城区', NULL, '130607', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (164, '2020-03-29 14:20:36.000', 1, 'system.region.area.130608', '清苑区', NULL, '130608', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (165, '2020-03-29 14:20:36.000', 1, 'system.region.area.130609', '徐水区', NULL, '130609', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (166, '2020-03-29 14:20:36.000', 1, 'system.region.area.130623', '涞水县', NULL, '130623', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (167, '2020-03-29 14:20:36.000', 1, 'system.region.area.130624', '阜平县', NULL, '130624', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (168, '2020-03-29 14:20:36.000', 1, 'system.region.area.130626', '定兴县', NULL, '130626', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (169, '2020-03-29 14:20:36.000', 1, 'system.region.area.130627', '唐县', NULL, '130627', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (170, '2020-03-29 14:20:36.000', 1, 'system.region.area.130628', '高阳县', NULL, '130628', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (171, '2020-03-29 14:20:36.000', 1, 'system.region.area.130629', '容城县', NULL, '130629', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (172, '2020-03-29 14:20:36.000', 1, 'system.region.area.130630', '涞源县', NULL, '130630', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (173, '2020-03-29 14:20:36.000', 1, 'system.region.area.130631', '望都县', NULL, '130631', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (174, '2020-03-29 14:20:36.000', 1, 'system.region.area.130632', '安新县', NULL, '130632', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (175, '2020-03-29 14:20:36.000', 1, 'system.region.area.130633', '易县', NULL, '130633', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (176, '2020-03-29 14:20:36.000', 1, 'system.region.area.130634', '曲阳县', NULL, '130634', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (177, '2020-03-29 14:20:36.000', 1, 'system.region.area.130635', '蠡县', NULL, '130635', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (178, '2020-03-29 14:20:36.000', 1, 'system.region.area.130636', '顺平县', NULL, '130636', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (179, '2020-03-29 14:20:36.000', 1, 'system.region.area.130637', '博野县', NULL, '130637', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (180, '2020-03-29 14:20:36.000', 1, 'system.region.area.130638', '雄县', NULL, '130638', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (181, '2020-03-29 14:20:36.000', 1, 'system.region.area.130681', '涿州市', NULL, '130681', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (182, '2020-03-29 14:20:36.000', 1, 'system.region.area.130682', '定州市', NULL, '130682', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (183, '2020-03-29 14:20:36.000', 1, 'system.region.area.130683', '安国市', NULL, '130683', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (184, '2020-03-29 14:20:36.000', 1, 'system.region.area.130684', '高碑店市', NULL, '130684', 30, NULL, 1, 16, 160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (185, '2020-03-29 14:20:36.000', 1, 'system.region.city.130700', '张家口市', 'area', '130700', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (186, '2020-03-29 14:20:36.000', 1, 'system.region.area.130702', '桥东区', NULL, '130702', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (187, '2020-03-29 14:20:36.000', 1, 'system.region.area.130703', '桥西区', NULL, '130703', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (188, '2020-03-29 14:20:36.000', 1, 'system.region.area.130705', '宣化区', NULL, '130705', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (189, '2020-03-29 14:20:36.000', 1, 'system.region.area.130706', '下花园区', NULL, '130706', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (190, '2020-03-29 14:20:36.000', 1, 'system.region.area.130708', '万全区', NULL, '130708', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (191, '2020-03-29 14:20:36.000', 1, 'system.region.area.130709', '崇礼区', NULL, '130709', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (192, '2020-03-29 14:20:36.000', 1, 'system.region.area.130722', '张北县', NULL, '130722', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (193, '2020-03-29 14:20:36.000', 1, 'system.region.area.130723', '康保县', NULL, '130723', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (194, '2020-03-29 14:20:36.000', 1, 'system.region.area.130724', '沽源县', NULL, '130724', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (195, '2020-03-29 14:20:36.000', 1, 'system.region.area.130725', '尚义县', NULL, '130725', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (196, '2020-03-29 14:20:36.000', 1, 'system.region.area.130726', '蔚县', NULL, '130726', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (197, '2020-03-29 14:20:36.000', 1, 'system.region.area.130727', '阳原县', NULL, '130727', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (198, '2020-03-29 14:20:36.000', 1, 'system.region.area.130728', '怀安县', NULL, '130728', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (199, '2020-03-29 14:20:36.000', 1, 'system.region.area.130730', '怀来县', NULL, '130730', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (200, '2020-03-29 14:20:36.000', 1, 'system.region.area.130731', '涿鹿县', NULL, '130731', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (201, '2020-03-29 14:20:36.000', 1, 'system.region.area.130732', '赤城县', NULL, '130732', 30, NULL, 1, 16, 185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (202, '2020-03-29 14:20:36.000', 1, 'system.region.city.130800', '承德市', 'area', '130800', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (203, '2020-03-29 14:20:36.000', 1, 'system.region.area.130802', '双桥区', NULL, '130802', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (204, '2020-03-29 14:20:36.000', 1, 'system.region.area.130803', '双滦区', NULL, '130803', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (205, '2020-03-29 14:20:36.000', 1, 'system.region.area.130804', '鹰手营子矿区', NULL, '130804', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (206, '2020-03-29 14:20:36.000', 1, 'system.region.area.130821', '承德县', NULL, '130821', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (207, '2020-03-29 14:20:36.000', 1, 'system.region.area.130822', '兴隆县', NULL, '130822', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (208, '2020-03-29 14:20:36.000', 1, 'system.region.area.130824', '滦平县', NULL, '130824', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (209, '2020-03-29 14:20:36.000', 1, 'system.region.area.130825', '隆化县', NULL, '130825', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (210, '2020-03-29 14:20:36.000', 1, 'system.region.area.130826', '丰宁满族自治县', NULL, '130826', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (211, '2020-03-29 14:20:36.000', 1, 'system.region.area.130827', '宽城满族自治县', NULL, '130827', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (212, '2020-03-29 14:20:36.000', 1, 'system.region.area.130828', '围场满族蒙古族自治县', NULL, '130828', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (213, '2020-03-29 14:20:36.000', 1, 'system.region.area.130881', '平泉市', NULL, '130881', 30, NULL, 1, 16, 202, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (214, '2020-03-29 14:20:36.000', 1, 'system.region.city.130900', '沧州市', 'area', '130900', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (215, '2020-03-29 14:20:36.000', 1, 'system.region.area.130902', '新华区', NULL, '130902', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (216, '2020-03-29 14:20:36.000', 1, 'system.region.area.130903', '运河区', NULL, '130903', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (217, '2020-03-29 14:20:36.000', 1, 'system.region.area.130921', '沧县', NULL, '130921', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (218, '2020-03-29 14:20:36.000', 1, 'system.region.area.130922', '青县', NULL, '130922', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (219, '2020-03-29 14:20:36.000', 1, 'system.region.area.130923', '东光县', NULL, '130923', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (220, '2020-03-29 14:20:36.000', 1, 'system.region.area.130924', '海兴县', NULL, '130924', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (221, '2020-03-29 14:20:36.000', 1, 'system.region.area.130925', '盐山县', NULL, '130925', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (222, '2020-03-29 14:20:36.000', 1, 'system.region.area.130926', '肃宁县', NULL, '130926', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (223, '2020-03-29 14:20:36.000', 1, 'system.region.area.130927', '南皮县', NULL, '130927', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (224, '2020-03-29 14:20:36.000', 1, 'system.region.area.130928', '吴桥县', NULL, '130928', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (225, '2020-03-29 14:20:36.000', 1, 'system.region.area.130929', '献县', NULL, '130929', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (226, '2020-03-29 14:20:36.000', 1, 'system.region.area.130930', '孟村回族自治县', NULL, '130930', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (227, '2020-03-29 14:20:36.000', 1, 'system.region.area.130981', '泊头市', NULL, '130981', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (228, '2020-03-29 14:20:36.000', 1, 'system.region.area.130982', '任丘市', NULL, '130982', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (229, '2020-03-29 14:20:36.000', 1, 'system.region.area.130983', '黄骅市', NULL, '130983', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (230, '2020-03-29 14:20:36.000', 1, 'system.region.area.130984', '河间市', NULL, '130984', 30, NULL, 1, 16, 214, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (231, '2020-03-29 14:20:36.000', 1, 'system.region.city.131000', '廊坊市', 'area', '131000', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (232, '2020-03-29 14:20:36.000', 1, 'system.region.area.131002', '安次区', NULL, '131002', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (233, '2020-03-29 14:20:36.000', 1, 'system.region.area.131003', '广阳区', NULL, '131003', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (234, '2020-03-29 14:20:36.000', 1, 'system.region.area.131022', '固安县', NULL, '131022', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (235, '2020-03-29 14:20:36.000', 1, 'system.region.area.131023', '永清县', NULL, '131023', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (236, '2020-03-29 14:20:36.000', 1, 'system.region.area.131024', '香河县', NULL, '131024', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (237, '2020-03-29 14:20:36.000', 1, 'system.region.area.131025', '大城县', NULL, '131025', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (238, '2020-03-29 14:20:36.000', 1, 'system.region.area.131026', '文安县', NULL, '131026', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (239, '2020-03-29 14:20:36.000', 1, 'system.region.area.131028', '大厂回族自治县', NULL, '131028', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (240, '2020-03-29 14:20:36.000', 1, 'system.region.area.131081', '霸州市', NULL, '131081', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (241, '2020-03-29 14:20:36.000', 1, 'system.region.area.131082', '三河市', NULL, '131082', 30, NULL, 1, 16, 231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (242, '2020-03-29 14:20:36.000', 1, 'system.region.city.131100', '衡水市', 'area', '131100', 30, NULL, 1, 15, 74, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (243, '2020-03-29 14:20:36.000', 1, 'system.region.area.131102', '桃城区', NULL, '131102', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (244, '2020-03-29 14:20:36.000', 1, 'system.region.area.131103', '冀州区', NULL, '131103', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (245, '2020-03-29 14:20:36.000', 1, 'system.region.area.131121', '枣强县', NULL, '131121', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (246, '2020-03-29 14:20:36.000', 1, 'system.region.area.131122', '武邑县', NULL, '131122', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (247, '2020-03-29 14:20:36.000', 1, 'system.region.area.131123', '武强县', NULL, '131123', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (248, '2020-03-29 14:20:36.000', 1, 'system.region.area.131124', '饶阳县', NULL, '131124', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (249, '2020-03-29 14:20:36.000', 1, 'system.region.area.131125', '安平县', NULL, '131125', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (250, '2020-03-29 14:20:36.000', 1, 'system.region.area.131126', '故城县', NULL, '131126', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (251, '2020-03-29 14:20:36.000', 1, 'system.region.area.131127', '景县', NULL, '131127', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (252, '2020-03-29 14:20:36.000', 1, 'system.region.area.131128', '阜城县', NULL, '131128', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (253, '2020-03-29 14:20:36.000', 1, 'system.region.area.131182', '深州市', NULL, '131182', 30, NULL, 1, 16, 242, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (254, '2020-03-29 14:20:36.000', 1, 'system.region.province.140000', '山西省', 'city', '140000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (255, '2020-03-29 14:20:36.000', 1, 'system.region.city.140100', '太原市', 'area', '140100', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (256, '2020-03-29 14:20:36.000', 1, 'system.region.area.140105', '小店区', NULL, '140105', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (257, '2020-03-29 14:20:36.000', 1, 'system.region.area.140106', '迎泽区', NULL, '140106', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (258, '2020-03-29 14:20:36.000', 1, 'system.region.area.140107', '杏花岭区', NULL, '140107', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (259, '2020-03-29 14:20:36.000', 1, 'system.region.area.140108', '尖草坪区', NULL, '140108', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (260, '2020-03-29 14:20:36.000', 1, 'system.region.area.140109', '万柏林区', NULL, '140109', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (261, '2020-03-29 14:20:36.000', 1, 'system.region.area.140110', '晋源区', NULL, '140110', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (262, '2020-03-29 14:20:36.000', 1, 'system.region.area.140121', '清徐县', NULL, '140121', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (263, '2020-03-29 14:20:36.000', 1, 'system.region.area.140122', '阳曲县', NULL, '140122', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (264, '2020-03-29 14:20:36.000', 1, 'system.region.area.140123', '娄烦县', NULL, '140123', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (265, '2020-03-29 14:20:36.000', 1, 'system.region.area.140181', '古交市', NULL, '140181', 30, NULL, 1, 16, 255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (266, '2020-03-29 14:20:36.000', 1, 'system.region.city.140200', '大同市', 'area', '140200', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (267, '2020-03-29 14:20:36.000', 1, 'system.region.area.140212', '新荣区', NULL, '140212', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (268, '2020-03-29 14:20:36.000', 1, 'system.region.area.140213', '平城区', NULL, '140213', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (269, '2020-03-29 14:20:36.000', 1, 'system.region.area.140214', '云冈区', NULL, '140214', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (270, '2020-03-29 14:20:36.000', 1, 'system.region.area.140215', '云州区', NULL, '140215', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (271, '2020-03-29 14:20:36.000', 1, 'system.region.area.140221', '阳高县', NULL, '140221', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (272, '2020-03-29 14:20:36.000', 1, 'system.region.area.140222', '天镇县', NULL, '140222', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (273, '2020-03-29 14:20:36.000', 1, 'system.region.area.140223', '广灵县', NULL, '140223', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (274, '2020-03-29 14:20:36.000', 1, 'system.region.area.140224', '灵丘县', NULL, '140224', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (275, '2020-03-29 14:20:36.000', 1, 'system.region.area.140225', '浑源县', NULL, '140225', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (276, '2020-03-29 14:20:36.000', 1, 'system.region.area.140226', '左云县', NULL, '140226', 30, NULL, 1, 16, 266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (277, '2020-03-29 14:20:36.000', 1, 'system.region.city.140300', '阳泉市', 'area', '140300', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (278, '2020-03-29 14:20:36.000', 1, 'system.region.area.140302', '城区', NULL, '140302', 30, NULL, 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (279, '2020-03-29 14:20:36.000', 1, 'system.region.area.140303', '矿区', NULL, '140303', 30, NULL, 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (280, '2020-03-29 14:20:36.000', 1, 'system.region.area.140311', '郊区', NULL, '140311', 30, NULL, 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (281, '2020-03-29 14:20:36.000', 1, 'system.region.area.140321', '平定县', NULL, '140321', 30, NULL, 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (282, '2020-03-29 14:20:36.000', 1, 'system.region.area.140322', '盂县', NULL, '140322', 30, NULL, 1, 16, 277, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (283, '2020-03-29 14:20:36.000', 1, 'system.region.city.140400', '长治市', 'area', '140400', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (284, '2020-03-29 14:20:36.000', 1, 'system.region.area.140403', '潞州区', NULL, '140403', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (285, '2020-03-29 14:20:36.000', 1, 'system.region.area.140404', '上党区', NULL, '140404', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (286, '2020-03-29 14:20:36.000', 1, 'system.region.area.140405', '屯留区', NULL, '140405', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (287, '2020-03-29 14:20:36.000', 1, 'system.region.area.140406', '潞城区', NULL, '140406', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (288, '2020-03-29 14:20:36.000', 1, 'system.region.area.140423', '襄垣县', NULL, '140423', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (289, '2020-03-29 14:20:36.000', 1, 'system.region.area.140425', '平顺县', NULL, '140425', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (290, '2020-03-29 14:20:36.000', 1, 'system.region.area.140426', '黎城县', NULL, '140426', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (291, '2020-03-29 14:20:36.000', 1, 'system.region.area.140427', '壶关县', NULL, '140427', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (292, '2020-03-29 14:20:36.000', 1, 'system.region.area.140428', '长子县', NULL, '140428', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (293, '2020-03-29 14:20:36.000', 1, 'system.region.area.140429', '武乡县', NULL, '140429', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (294, '2020-03-29 14:20:36.000', 1, 'system.region.area.140430', '沁县', NULL, '140430', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (295, '2020-03-29 14:20:36.000', 1, 'system.region.area.140431', '沁源县', NULL, '140431', 30, NULL, 1, 16, 283, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (296, '2020-03-29 14:20:36.000', 1, 'system.region.city.140500', '晋城市', 'area', '140500', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (297, '2020-03-29 14:20:36.000', 1, 'system.region.area.140502', '城区', NULL, '140502', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (298, '2020-03-29 14:20:36.000', 1, 'system.region.area.140521', '沁水县', NULL, '140521', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (299, '2020-03-29 14:20:36.000', 1, 'system.region.area.140522', '阳城县', NULL, '140522', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (300, '2020-03-29 14:20:36.000', 1, 'system.region.area.140524', '陵川县', NULL, '140524', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (301, '2020-03-29 14:20:36.000', 1, 'system.region.area.140525', '泽州县', NULL, '140525', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (302, '2020-03-29 14:20:36.000', 1, 'system.region.area.140581', '高平市', NULL, '140581', 30, NULL, 1, 16, 296, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (303, '2020-03-29 14:20:36.000', 1, 'system.region.city.140600', '朔州市', 'area', '140600', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (304, '2020-03-29 14:20:36.000', 1, 'system.region.area.140602', '朔城区', NULL, '140602', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (305, '2020-03-29 14:20:36.000', 1, 'system.region.area.140603', '平鲁区', NULL, '140603', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (306, '2020-03-29 14:20:36.000', 1, 'system.region.area.140621', '山阴县', NULL, '140621', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (307, '2020-03-29 14:20:36.000', 1, 'system.region.area.140622', '应县', NULL, '140622', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (308, '2020-03-29 14:20:36.000', 1, 'system.region.area.140623', '右玉县', NULL, '140623', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (309, '2020-03-29 14:20:36.000', 1, 'system.region.area.140681', '怀仁市', NULL, '140681', 30, NULL, 1, 16, 303, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (310, '2020-03-29 14:20:36.000', 1, 'system.region.city.140700', '晋中市', 'area', '140700', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (311, '2020-03-29 14:20:36.000', 1, 'system.region.area.140702', '榆次区', NULL, '140702', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (312, '2020-03-29 14:20:36.000', 1, 'system.region.area.140703', '太谷区', NULL, '140703', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (313, '2020-03-29 14:20:36.000', 1, 'system.region.area.140721', '榆社县', NULL, '140721', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (314, '2020-03-29 14:20:36.000', 1, 'system.region.area.140722', '左权县', NULL, '140722', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (315, '2020-03-29 14:20:36.000', 1, 'system.region.area.140723', '和顺县', NULL, '140723', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (316, '2020-03-29 14:20:36.000', 1, 'system.region.area.140724', '昔阳县', NULL, '140724', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (317, '2020-03-29 14:20:36.000', 1, 'system.region.area.140725', '寿阳县', NULL, '140725', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (318, '2020-03-29 14:20:36.000', 1, 'system.region.area.140727', '祁县', NULL, '140727', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (319, '2020-03-29 14:20:36.000', 1, 'system.region.area.140728', '平遥县', NULL, '140728', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (320, '2020-03-29 14:20:36.000', 1, 'system.region.area.140729', '灵石县', NULL, '140729', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (321, '2020-03-29 14:20:36.000', 1, 'system.region.area.140781', '介休市', NULL, '140781', 30, NULL, 1, 16, 310, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (322, '2020-03-29 14:20:36.000', 1, 'system.region.city.140800', '运城市', 'area', '140800', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (323, '2020-03-29 14:20:36.000', 1, 'system.region.area.140802', '盐湖区', NULL, '140802', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (324, '2020-03-29 14:20:36.000', 1, 'system.region.area.140821', '临猗县', NULL, '140821', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (325, '2020-03-29 14:20:36.000', 1, 'system.region.area.140822', '万荣县', NULL, '140822', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (326, '2020-03-29 14:20:36.000', 1, 'system.region.area.140823', '闻喜县', NULL, '140823', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (327, '2020-03-29 14:20:36.000', 1, 'system.region.area.140824', '稷山县', NULL, '140824', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (328, '2020-03-29 14:20:36.000', 1, 'system.region.area.140825', '新绛县', NULL, '140825', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (329, '2020-03-29 14:20:36.000', 1, 'system.region.area.140826', '绛县', NULL, '140826', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (330, '2020-03-29 14:20:36.000', 1, 'system.region.area.140827', '垣曲县', NULL, '140827', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (331, '2020-03-29 14:20:36.000', 1, 'system.region.area.140828', '夏县', NULL, '140828', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (332, '2020-03-29 14:20:36.000', 1, 'system.region.area.140829', '平陆县', NULL, '140829', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (333, '2020-03-29 14:20:36.000', 1, 'system.region.area.140830', '芮城县', NULL, '140830', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (334, '2020-03-29 14:20:36.000', 1, 'system.region.area.140881', '永济市', NULL, '140881', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (335, '2020-03-29 14:20:36.000', 1, 'system.region.area.140882', '河津市', NULL, '140882', 30, NULL, 1, 16, 322, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (336, '2020-03-29 14:20:36.000', 1, 'system.region.city.140900', '忻州市', 'area', '140900', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (337, '2020-03-29 14:20:36.000', 1, 'system.region.area.140902', '忻府区', NULL, '140902', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (338, '2020-03-29 14:20:36.000', 1, 'system.region.area.140921', '定襄县', NULL, '140921', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (339, '2020-03-29 14:20:36.000', 1, 'system.region.area.140922', '五台县', NULL, '140922', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (340, '2020-03-29 14:20:36.000', 1, 'system.region.area.140923', '代县', NULL, '140923', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (341, '2020-03-29 14:20:36.000', 1, 'system.region.area.140924', '繁峙县', NULL, '140924', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (342, '2020-03-29 14:20:36.000', 1, 'system.region.area.140925', '宁武县', NULL, '140925', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (343, '2020-03-29 14:20:36.000', 1, 'system.region.area.140926', '静乐县', NULL, '140926', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (344, '2020-03-29 14:20:36.000', 1, 'system.region.area.140927', '神池县', NULL, '140927', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (345, '2020-03-29 14:20:36.000', 1, 'system.region.area.140928', '五寨县', NULL, '140928', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (346, '2020-03-29 14:20:36.000', 1, 'system.region.area.140929', '岢岚县', NULL, '140929', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (347, '2020-03-29 14:20:36.000', 1, 'system.region.area.140930', '河曲县', NULL, '140930', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (348, '2020-03-29 14:20:36.000', 1, 'system.region.area.140931', '保德县', NULL, '140931', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (349, '2020-03-29 14:20:36.000', 1, 'system.region.area.140932', '偏关县', NULL, '140932', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (350, '2020-03-29 14:20:36.000', 1, 'system.region.area.140981', '原平市', NULL, '140981', 30, NULL, 1, 16, 336, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (351, '2020-03-29 14:20:36.000', 1, 'system.region.city.141000', '临汾市', 'area', '141000', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (352, '2020-03-29 14:20:36.000', 1, 'system.region.area.141002', '尧都区', NULL, '141002', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (353, '2020-03-29 14:20:36.000', 1, 'system.region.area.141021', '曲沃县', NULL, '141021', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (354, '2020-03-29 14:20:36.000', 1, 'system.region.area.141022', '翼城县', NULL, '141022', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (355, '2020-03-29 14:20:36.000', 1, 'system.region.area.141023', '襄汾县', NULL, '141023', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (356, '2020-03-29 14:20:36.000', 1, 'system.region.area.141024', '洪洞县', NULL, '141024', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (357, '2020-03-29 14:20:36.000', 1, 'system.region.area.141025', '古县', NULL, '141025', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (358, '2020-03-29 14:20:36.000', 1, 'system.region.area.141026', '安泽县', NULL, '141026', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (359, '2020-03-29 14:20:36.000', 1, 'system.region.area.141027', '浮山县', NULL, '141027', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (360, '2020-03-29 14:20:36.000', 1, 'system.region.area.141028', '吉县', NULL, '141028', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (361, '2020-03-29 14:20:36.000', 1, 'system.region.area.141029', '乡宁县', NULL, '141029', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (362, '2020-03-29 14:20:36.000', 1, 'system.region.area.141030', '大宁县', NULL, '141030', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (363, '2020-03-29 14:20:36.000', 1, 'system.region.area.141031', '隰县', NULL, '141031', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (364, '2020-03-29 14:20:36.000', 1, 'system.region.area.141032', '永和县', NULL, '141032', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (365, '2020-03-29 14:20:36.000', 1, 'system.region.area.141033', '蒲县', NULL, '141033', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (366, '2020-03-29 14:20:36.000', 1, 'system.region.area.141034', '汾西县', NULL, '141034', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (367, '2020-03-29 14:20:36.000', 1, 'system.region.area.141081', '侯马市', NULL, '141081', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (368, '2020-03-29 14:20:36.000', 1, 'system.region.area.141082', '霍州市', NULL, '141082', 30, NULL, 1, 16, 351, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (369, '2020-03-29 14:20:36.000', 1, 'system.region.city.141100', '吕梁市', 'area', '141100', 30, NULL, 1, 15, 254, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (370, '2020-03-29 14:20:36.000', 1, 'system.region.area.141102', '离石区', NULL, '141102', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (371, '2020-03-29 14:20:36.000', 1, 'system.region.area.141121', '文水县', NULL, '141121', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (372, '2020-03-29 14:20:36.000', 1, 'system.region.area.141122', '交城县', NULL, '141122', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (373, '2020-03-29 14:20:36.000', 1, 'system.region.area.141123', '兴县', NULL, '141123', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (374, '2020-03-29 14:20:36.000', 1, 'system.region.area.141124', '临县', NULL, '141124', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (375, '2020-03-29 14:20:36.000', 1, 'system.region.area.141125', '柳林县', NULL, '141125', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (376, '2020-03-29 14:20:36.000', 1, 'system.region.area.141126', '石楼县', NULL, '141126', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (377, '2020-03-29 14:20:36.000', 1, 'system.region.area.141127', '岚县', NULL, '141127', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (378, '2020-03-29 14:20:36.000', 1, 'system.region.area.141128', '方山县', NULL, '141128', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (379, '2020-03-29 14:20:36.000', 1, 'system.region.area.141129', '中阳县', NULL, '141129', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (380, '2020-03-29 14:20:36.000', 1, 'system.region.area.141130', '交口县', NULL, '141130', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (381, '2020-03-29 14:20:36.000', 1, 'system.region.area.141181', '孝义市', NULL, '141181', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (382, '2020-03-29 14:20:36.000', 1, 'system.region.area.141182', '汾阳市', NULL, '141182', 30, NULL, 1, 16, 369, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (383, '2020-03-29 14:20:36.000', 1, 'system.region.province.150000', '内蒙古自治区', 'city', '150000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (384, '2020-03-29 14:20:36.000', 1, 'system.region.city.150100', '呼和浩特市', 'area', '150100', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (385, '2020-03-29 14:20:36.000', 1, 'system.region.area.150102', '新城区', NULL, '150102', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (386, '2020-03-29 14:20:36.000', 1, 'system.region.area.150103', '回民区', NULL, '150103', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (387, '2020-03-29 14:20:36.000', 1, 'system.region.area.150104', '玉泉区', NULL, '150104', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (388, '2020-03-29 14:20:36.000', 1, 'system.region.area.150105', '赛罕区', NULL, '150105', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (389, '2020-03-29 14:20:36.000', 1, 'system.region.area.150121', '土默特左旗', NULL, '150121', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (390, '2020-03-29 14:20:36.000', 1, 'system.region.area.150122', '托克托县', NULL, '150122', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (391, '2020-03-29 14:20:36.000', 1, 'system.region.area.150123', '和林格尔县', NULL, '150123', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (392, '2020-03-29 14:20:36.000', 1, 'system.region.area.150124', '清水河县', NULL, '150124', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (393, '2020-03-29 14:20:36.000', 1, 'system.region.area.150125', '武川县', NULL, '150125', 30, NULL, 1, 16, 384, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (394, '2020-03-29 14:20:36.000', 1, 'system.region.city.150200', '包头市', 'area', '150200', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (395, '2020-03-29 14:20:36.000', 1, 'system.region.area.150202', '东河区', NULL, '150202', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (396, '2020-03-29 14:20:36.000', 1, 'system.region.area.150203', '昆都仑区', NULL, '150203', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (397, '2020-03-29 14:20:36.000', 1, 'system.region.area.150204', '青山区', NULL, '150204', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (398, '2020-03-29 14:20:36.000', 1, 'system.region.area.150205', '石拐区', NULL, '150205', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (399, '2020-03-29 14:20:36.000', 1, 'system.region.area.150206', '白云鄂博矿区', NULL, '150206', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (400, '2020-03-29 14:20:36.000', 1, 'system.region.area.150207', '九原区', NULL, '150207', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (401, '2020-03-29 14:20:36.000', 1, 'system.region.area.150221', '土默特右旗', NULL, '150221', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (402, '2020-03-29 14:20:36.000', 1, 'system.region.area.150222', '固阳县', NULL, '150222', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (403, '2020-03-29 14:20:36.000', 1, 'system.region.area.150223', '达尔罕茂明安联合旗', NULL, '150223', 30, NULL, 1, 16, 394, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (404, '2020-03-29 14:20:36.000', 1, 'system.region.city.150300', '乌海市', 'area', '150300', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (405, '2020-03-29 14:20:36.000', 1, 'system.region.area.150302', '海勃湾区', NULL, '150302', 30, NULL, 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (406, '2020-03-29 14:20:36.000', 1, 'system.region.area.150303', '海南区', NULL, '150303', 30, NULL, 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (407, '2020-03-29 14:20:36.000', 1, 'system.region.area.150304', '乌达区', NULL, '150304', 30, NULL, 1, 16, 404, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (408, '2020-03-29 14:20:36.000', 1, 'system.region.city.150400', '赤峰市', 'area', '150400', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (409, '2020-03-29 14:20:36.000', 1, 'system.region.area.150402', '红山区', NULL, '150402', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (410, '2020-03-29 14:20:36.000', 1, 'system.region.area.150403', '元宝山区', NULL, '150403', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (411, '2020-03-29 14:20:36.000', 1, 'system.region.area.150404', '松山区', NULL, '150404', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (412, '2020-03-29 14:20:36.000', 1, 'system.region.area.150421', '阿鲁科尔沁旗', NULL, '150421', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (413, '2020-03-29 14:20:36.000', 1, 'system.region.area.150422', '巴林左旗', NULL, '150422', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (414, '2020-03-29 14:20:36.000', 1, 'system.region.area.150423', '巴林右旗', NULL, '150423', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (415, '2020-03-29 14:20:36.000', 1, 'system.region.area.150424', '林西县', NULL, '150424', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (416, '2020-03-29 14:20:36.000', 1, 'system.region.area.150425', '克什克腾旗', NULL, '150425', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (417, '2020-03-29 14:20:36.000', 1, 'system.region.area.150426', '翁牛特旗', NULL, '150426', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (418, '2020-03-29 14:20:36.000', 1, 'system.region.area.150428', '喀喇沁旗', NULL, '150428', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (419, '2020-03-29 14:20:36.000', 1, 'system.region.area.150429', '宁城县', NULL, '150429', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (420, '2020-03-29 14:20:36.000', 1, 'system.region.area.150430', '敖汉旗', NULL, '150430', 30, NULL, 1, 16, 408, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (421, '2020-03-29 14:20:36.000', 1, 'system.region.city.150500', '通辽市', 'area', '150500', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (422, '2020-03-29 14:20:36.000', 1, 'system.region.area.150502', '科尔沁区', NULL, '150502', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (423, '2020-03-29 14:20:36.000', 1, 'system.region.area.150521', '科尔沁左翼中旗', NULL, '150521', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (424, '2020-03-29 14:20:36.000', 1, 'system.region.area.150522', '科尔沁左翼后旗', NULL, '150522', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (425, '2020-03-29 14:20:36.000', 1, 'system.region.area.150523', '开鲁县', NULL, '150523', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (426, '2020-03-29 14:20:36.000', 1, 'system.region.area.150524', '库伦旗', NULL, '150524', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (427, '2020-03-29 14:20:36.000', 1, 'system.region.area.150525', '奈曼旗', NULL, '150525', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (428, '2020-03-29 14:20:36.000', 1, 'system.region.area.150526', '扎鲁特旗', NULL, '150526', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (429, '2020-03-29 14:20:36.000', 1, 'system.region.area.150581', '霍林郭勒市', NULL, '150581', 30, NULL, 1, 16, 421, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (430, '2020-03-29 14:20:36.000', 1, 'system.region.city.150600', '鄂尔多斯市', 'area', '150600', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (431, '2020-03-29 14:20:36.000', 1, 'system.region.area.150602', '东胜区', NULL, '150602', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (432, '2020-03-29 14:20:36.000', 1, 'system.region.area.150603', '康巴什区', NULL, '150603', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (433, '2020-03-29 14:20:36.000', 1, 'system.region.area.150621', '达拉特旗', NULL, '150621', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (434, '2020-03-29 14:20:36.000', 1, 'system.region.area.150622', '准格尔旗', NULL, '150622', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (435, '2020-03-29 14:20:36.000', 1, 'system.region.area.150623', '鄂托克前旗', NULL, '150623', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (436, '2020-03-29 14:20:36.000', 1, 'system.region.area.150624', '鄂托克旗', NULL, '150624', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (437, '2020-03-29 14:20:36.000', 1, 'system.region.area.150625', '杭锦旗', NULL, '150625', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (438, '2020-03-29 14:20:36.000', 1, 'system.region.area.150626', '乌审旗', NULL, '150626', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (439, '2020-03-29 14:20:36.000', 1, 'system.region.area.150627', '伊金霍洛旗', NULL, '150627', 30, NULL, 1, 16, 430, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (440, '2020-03-29 14:20:36.000', 1, 'system.region.city.150700', '呼伦贝尔市', 'area', '150700', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (441, '2020-03-29 14:20:36.000', 1, 'system.region.area.150702', '海拉尔区', NULL, '150702', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (442, '2020-03-29 14:20:36.000', 1, 'system.region.area.150703', '扎赉诺尔区', NULL, '150703', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (443, '2020-03-29 14:20:36.000', 1, 'system.region.area.150721', '阿荣旗', NULL, '150721', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (444, '2020-03-29 14:20:36.000', 1, 'system.region.area.150722', '莫力达瓦达斡尔族自治旗', NULL, '150722', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (445, '2020-03-29 14:20:36.000', 1, 'system.region.area.150723', '鄂伦春自治旗', NULL, '150723', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (446, '2020-03-29 14:20:36.000', 1, 'system.region.area.150724', '鄂温克族自治旗', NULL, '150724', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (447, '2020-03-29 14:20:36.000', 1, 'system.region.area.150725', '陈巴尔虎旗', NULL, '150725', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (448, '2020-03-29 14:20:36.000', 1, 'system.region.area.150726', '新巴尔虎左旗', NULL, '150726', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (449, '2020-03-29 14:20:36.000', 1, 'system.region.area.150727', '新巴尔虎右旗', NULL, '150727', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (450, '2020-03-29 14:20:36.000', 1, 'system.region.area.150781', '满洲里市', NULL, '150781', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (451, '2020-03-29 14:20:36.000', 1, 'system.region.area.150782', '牙克石市', NULL, '150782', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (452, '2020-03-29 14:20:36.000', 1, 'system.region.area.150783', '扎兰屯市', NULL, '150783', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (453, '2020-03-29 14:20:36.000', 1, 'system.region.area.150784', '额尔古纳市', NULL, '150784', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (454, '2020-03-29 14:20:36.000', 1, 'system.region.area.150785', '根河市', NULL, '150785', 30, NULL, 1, 16, 440, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (455, '2020-03-29 14:20:36.000', 1, 'system.region.city.150800', '巴彦淖尔市', 'area', '150800', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (456, '2020-03-29 14:20:36.000', 1, 'system.region.area.150802', '临河区', NULL, '150802', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (457, '2020-03-29 14:20:36.000', 1, 'system.region.area.150821', '五原县', NULL, '150821', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (458, '2020-03-29 14:20:36.000', 1, 'system.region.area.150822', '磴口县', NULL, '150822', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (459, '2020-03-29 14:20:36.000', 1, 'system.region.area.150823', '乌拉特前旗', NULL, '150823', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (460, '2020-03-29 14:20:36.000', 1, 'system.region.area.150824', '乌拉特中旗', NULL, '150824', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (461, '2020-03-29 14:20:36.000', 1, 'system.region.area.150825', '乌拉特后旗', NULL, '150825', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (462, '2020-03-29 14:20:36.000', 1, 'system.region.area.150826', '杭锦后旗', NULL, '150826', 30, NULL, 1, 16, 455, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (463, '2020-03-29 14:20:36.000', 1, 'system.region.city.150900', '乌兰察布市', 'area', '150900', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (464, '2020-03-29 14:20:36.000', 1, 'system.region.area.150902', '集宁区', NULL, '150902', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (465, '2020-03-29 14:20:36.000', 1, 'system.region.area.150921', '卓资县', NULL, '150921', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (466, '2020-03-29 14:20:36.000', 1, 'system.region.area.150922', '化德县', NULL, '150922', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (467, '2020-03-29 14:20:36.000', 1, 'system.region.area.150923', '商都县', NULL, '150923', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (468, '2020-03-29 14:20:36.000', 1, 'system.region.area.150924', '兴和县', NULL, '150924', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (469, '2020-03-29 14:20:36.000', 1, 'system.region.area.150925', '凉城县', NULL, '150925', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (470, '2020-03-29 14:20:36.000', 1, 'system.region.area.150926', '察哈尔右翼前旗', NULL, '150926', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (471, '2020-03-29 14:20:36.000', 1, 'system.region.area.150927', '察哈尔右翼中旗', NULL, '150927', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (472, '2020-03-29 14:20:36.000', 1, 'system.region.area.150928', '察哈尔右翼后旗', NULL, '150928', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (473, '2020-03-29 14:20:36.000', 1, 'system.region.area.150929', '四子王旗', NULL, '150929', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (474, '2020-03-29 14:20:36.000', 1, 'system.region.area.150981', '丰镇市', NULL, '150981', 30, NULL, 1, 16, 463, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (475, '2020-03-29 14:20:36.000', 1, 'system.region.city.152200', '兴安盟', 'area', '152200', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (476, '2020-03-29 14:20:36.000', 1, 'system.region.area.152201', '乌兰浩特市', NULL, '152201', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (477, '2020-03-29 14:20:36.000', 1, 'system.region.area.152202', '阿尔山市', NULL, '152202', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (478, '2020-03-29 14:20:36.000', 1, 'system.region.area.152221', '科尔沁右翼前旗', NULL, '152221', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (479, '2020-03-29 14:20:36.000', 1, 'system.region.area.152222', '科尔沁右翼中旗', NULL, '152222', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (480, '2020-03-29 14:20:36.000', 1, 'system.region.area.152223', '扎赉特旗', NULL, '152223', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (481, '2020-03-29 14:20:36.000', 1, 'system.region.area.152224', '突泉县', NULL, '152224', 30, NULL, 1, 16, 475, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (482, '2020-03-29 14:20:36.000', 1, 'system.region.city.152500', '锡林郭勒盟', 'area', '152500', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (483, '2020-03-29 14:20:36.000', 1, 'system.region.area.152501', '二连浩特市', NULL, '152501', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (484, '2020-03-29 14:20:36.000', 1, 'system.region.area.152502', '锡林浩特市', NULL, '152502', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (485, '2020-03-29 14:20:36.000', 1, 'system.region.area.152522', '阿巴嘎旗', NULL, '152522', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (486, '2020-03-29 14:20:36.000', 1, 'system.region.area.152523', '苏尼特左旗', NULL, '152523', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (487, '2020-03-29 14:20:36.000', 1, 'system.region.area.152524', '苏尼特右旗', NULL, '152524', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (488, '2020-03-29 14:20:36.000', 1, 'system.region.area.152525', '东乌珠穆沁旗', NULL, '152525', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (489, '2020-03-29 14:20:36.000', 1, 'system.region.area.152526', '西乌珠穆沁旗', NULL, '152526', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (490, '2020-03-29 14:20:36.000', 1, 'system.region.area.152527', '太仆寺旗', NULL, '152527', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (491, '2020-03-29 14:20:36.000', 1, 'system.region.area.152528', '镶黄旗', NULL, '152528', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (492, '2020-03-29 14:20:36.000', 1, 'system.region.area.152529', '正镶白旗', NULL, '152529', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (493, '2020-03-29 14:20:36.000', 1, 'system.region.area.152530', '正蓝旗', NULL, '152530', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (494, '2020-03-29 14:20:36.000', 1, 'system.region.area.152531', '多伦县', NULL, '152531', 30, NULL, 1, 16, 482, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (495, '2020-03-29 14:20:36.000', 1, 'system.region.city.152900', '阿拉善盟', 'area', '152900', 30, NULL, 1, 15, 383, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (496, '2020-03-29 14:20:36.000', 1, 'system.region.area.152921', '阿拉善左旗', NULL, '152921', 30, NULL, 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (497, '2020-03-29 14:20:36.000', 1, 'system.region.area.152922', '阿拉善右旗', NULL, '152922', 30, NULL, 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (498, '2020-03-29 14:20:36.000', 1, 'system.region.area.152923', '额济纳旗', NULL, '152923', 30, NULL, 1, 16, 495, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (499, '2020-03-29 14:20:36.000', 1, 'system.region.province.210000', '辽宁省', 'city', '210000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (500, '2020-03-29 14:20:36.000', 1, 'system.region.city.210100', '沈阳市', 'area', '210100', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (501, '2020-03-29 14:20:36.000', 1, 'system.region.area.210102', '和平区', NULL, '210102', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (502, '2020-03-29 14:20:36.000', 1, 'system.region.area.210103', '沈河区', NULL, '210103', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (503, '2020-03-29 14:20:36.000', 1, 'system.region.area.210104', '大东区', NULL, '210104', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (504, '2020-03-29 14:20:36.000', 1, 'system.region.area.210105', '皇姑区', NULL, '210105', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (505, '2020-03-29 14:20:36.000', 1, 'system.region.area.210106', '铁西区', NULL, '210106', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (506, '2020-03-29 14:20:36.000', 1, 'system.region.area.210111', '苏家屯区', NULL, '210111', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (507, '2020-03-29 14:20:36.000', 1, 'system.region.area.210112', '浑南区', NULL, '210112', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (508, '2020-03-29 14:20:36.000', 1, 'system.region.area.210113', '沈北新区', NULL, '210113', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (509, '2020-03-29 14:20:36.000', 1, 'system.region.area.210114', '于洪区', NULL, '210114', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (510, '2020-03-29 14:20:36.000', 1, 'system.region.area.210115', '辽中区', NULL, '210115', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (511, '2020-03-29 14:20:36.000', 1, 'system.region.area.210123', '康平县', NULL, '210123', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (512, '2020-03-29 14:20:36.000', 1, 'system.region.area.210124', '法库县', NULL, '210124', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (513, '2020-03-29 14:20:36.000', 1, 'system.region.area.210181', '新民市', NULL, '210181', 30, NULL, 1, 16, 500, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (514, '2020-03-29 14:20:36.000', 1, 'system.region.city.210200', '大连市', 'area', '210200', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (515, '2020-03-29 14:20:36.000', 1, 'system.region.area.210202', '中山区', NULL, '210202', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (516, '2020-03-29 14:20:36.000', 1, 'system.region.area.210203', '西岗区', NULL, '210203', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (517, '2020-03-29 14:20:36.000', 1, 'system.region.area.210204', '沙河口区', NULL, '210204', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (518, '2020-03-29 14:20:36.000', 1, 'system.region.area.210211', '甘井子区', NULL, '210211', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (519, '2020-03-29 14:20:36.000', 1, 'system.region.area.210212', '旅顺口区', NULL, '210212', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (520, '2020-03-29 14:20:36.000', 1, 'system.region.area.210213', '金州区', NULL, '210213', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (521, '2020-03-29 14:20:36.000', 1, 'system.region.area.210214', '普兰店区', NULL, '210214', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (522, '2020-03-29 14:20:36.000', 1, 'system.region.area.210224', '长海县', NULL, '210224', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (523, '2020-03-29 14:20:36.000', 1, 'system.region.area.210281', '瓦房店市', NULL, '210281', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (524, '2020-03-29 14:20:36.000', 1, 'system.region.area.210283', '庄河市', NULL, '210283', 30, NULL, 1, 16, 514, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (525, '2020-03-29 14:20:36.000', 1, 'system.region.city.210300', '鞍山市', 'area', '210300', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (526, '2020-03-29 14:20:36.000', 1, 'system.region.area.210302', '铁东区', NULL, '210302', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (527, '2020-03-29 14:20:36.000', 1, 'system.region.area.210303', '铁西区', NULL, '210303', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (528, '2020-03-29 14:20:36.000', 1, 'system.region.area.210304', '立山区', NULL, '210304', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (529, '2020-03-29 14:20:36.000', 1, 'system.region.area.210311', '千山区', NULL, '210311', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (530, '2020-03-29 14:20:36.000', 1, 'system.region.area.210321', '台安县', NULL, '210321', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (531, '2020-03-29 14:20:36.000', 1, 'system.region.area.210323', '岫岩满族自治县', NULL, '210323', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (532, '2020-03-29 14:20:36.000', 1, 'system.region.area.210381', '海城市', NULL, '210381', 30, NULL, 1, 16, 525, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (533, '2020-03-29 14:20:36.000', 1, 'system.region.city.210400', '抚顺市', 'area', '210400', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (534, '2020-03-29 14:20:36.000', 1, 'system.region.area.210402', '新抚区', NULL, '210402', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (535, '2020-03-29 14:20:36.000', 1, 'system.region.area.210403', '东洲区', NULL, '210403', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (536, '2020-03-29 14:20:36.000', 1, 'system.region.area.210404', '望花区', NULL, '210404', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (537, '2020-03-29 14:20:36.000', 1, 'system.region.area.210411', '顺城区', NULL, '210411', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (538, '2020-03-29 14:20:36.000', 1, 'system.region.area.210421', '抚顺县', NULL, '210421', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (539, '2020-03-29 14:20:36.000', 1, 'system.region.area.210422', '新宾满族自治县', NULL, '210422', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (540, '2020-03-29 14:20:36.000', 1, 'system.region.area.210423', '清原满族自治县', NULL, '210423', 30, NULL, 1, 16, 533, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (541, '2020-03-29 14:20:36.000', 1, 'system.region.city.210500', '本溪市', 'area', '210500', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (542, '2020-03-29 14:20:36.000', 1, 'system.region.area.210502', '平山区', NULL, '210502', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (543, '2020-03-29 14:20:36.000', 1, 'system.region.area.210503', '溪湖区', NULL, '210503', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (544, '2020-03-29 14:20:36.000', 1, 'system.region.area.210504', '明山区', NULL, '210504', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (545, '2020-03-29 14:20:36.000', 1, 'system.region.area.210505', '南芬区', NULL, '210505', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (546, '2020-03-29 14:20:36.000', 1, 'system.region.area.210521', '本溪满族自治县', NULL, '210521', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (547, '2020-03-29 14:20:36.000', 1, 'system.region.area.210522', '桓仁满族自治县', NULL, '210522', 30, NULL, 1, 16, 541, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (548, '2020-03-29 14:20:36.000', 1, 'system.region.city.210600', '丹东市', 'area', '210600', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (549, '2020-03-29 14:20:36.000', 1, 'system.region.area.210602', '元宝区', NULL, '210602', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (550, '2020-03-29 14:20:36.000', 1, 'system.region.area.210603', '振兴区', NULL, '210603', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (551, '2020-03-29 14:20:36.000', 1, 'system.region.area.210604', '振安区', NULL, '210604', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (552, '2020-03-29 14:20:36.000', 1, 'system.region.area.210624', '宽甸满族自治县', NULL, '210624', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (553, '2020-03-29 14:20:36.000', 1, 'system.region.area.210681', '东港市', NULL, '210681', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (554, '2020-03-29 14:20:36.000', 1, 'system.region.area.210682', '凤城市', NULL, '210682', 30, NULL, 1, 16, 548, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (555, '2020-03-29 14:20:36.000', 1, 'system.region.city.210700', '锦州市', 'area', '210700', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (556, '2020-03-29 14:20:36.000', 1, 'system.region.area.210702', '古塔区', NULL, '210702', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (557, '2020-03-29 14:20:36.000', 1, 'system.region.area.210703', '凌河区', NULL, '210703', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (558, '2020-03-29 14:20:36.000', 1, 'system.region.area.210711', '太和区', NULL, '210711', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (559, '2020-03-29 14:20:36.000', 1, 'system.region.area.210726', '黑山县', NULL, '210726', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (560, '2020-03-29 14:20:36.000', 1, 'system.region.area.210727', '义县', NULL, '210727', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (561, '2020-03-29 14:20:36.000', 1, 'system.region.area.210781', '凌海市', NULL, '210781', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (562, '2020-03-29 14:20:36.000', 1, 'system.region.area.210782', '北镇市', NULL, '210782', 30, NULL, 1, 16, 555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (563, '2020-03-29 14:20:36.000', 1, 'system.region.city.210800', '营口市', 'area', '210800', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (564, '2020-03-29 14:20:36.000', 1, 'system.region.area.210802', '站前区', NULL, '210802', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (565, '2020-03-29 14:20:36.000', 1, 'system.region.area.210803', '西市区', NULL, '210803', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (566, '2020-03-29 14:20:36.000', 1, 'system.region.area.210804', '鲅鱼圈区', NULL, '210804', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (567, '2020-03-29 14:20:36.000', 1, 'system.region.area.210811', '老边区', NULL, '210811', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (568, '2020-03-29 14:20:36.000', 1, 'system.region.area.210881', '盖州市', NULL, '210881', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (569, '2020-03-29 14:20:36.000', 1, 'system.region.area.210882', '大石桥市', NULL, '210882', 30, NULL, 1, 16, 563, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (570, '2020-03-29 14:20:36.000', 1, 'system.region.city.210900', '阜新市', 'area', '210900', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (571, '2020-03-29 14:20:36.000', 1, 'system.region.area.210902', '海州区', NULL, '210902', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (572, '2020-03-29 14:20:36.000', 1, 'system.region.area.210903', '新邱区', NULL, '210903', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (573, '2020-03-29 14:20:36.000', 1, 'system.region.area.210904', '太平区', NULL, '210904', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (574, '2020-03-29 14:20:36.000', 1, 'system.region.area.210905', '清河门区', NULL, '210905', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (575, '2020-03-29 14:20:36.000', 1, 'system.region.area.210911', '细河区', NULL, '210911', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (576, '2020-03-29 14:20:36.000', 1, 'system.region.area.210921', '阜新蒙古族自治县', NULL, '210921', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (577, '2020-03-29 14:20:36.000', 1, 'system.region.area.210922', '彰武县', NULL, '210922', 30, NULL, 1, 16, 570, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (578, '2020-03-29 14:20:36.000', 1, 'system.region.city.211000', '辽阳市', 'area', '211000', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (579, '2020-03-29 14:20:36.000', 1, 'system.region.area.211002', '白塔区', NULL, '211002', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (580, '2020-03-29 14:20:36.000', 1, 'system.region.area.211003', '文圣区', NULL, '211003', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (581, '2020-03-29 14:20:36.000', 1, 'system.region.area.211004', '宏伟区', NULL, '211004', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (582, '2020-03-29 14:20:36.000', 1, 'system.region.area.211005', '弓长岭区', NULL, '211005', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (583, '2020-03-29 14:20:36.000', 1, 'system.region.area.211011', '太子河区', NULL, '211011', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (584, '2020-03-29 14:20:36.000', 1, 'system.region.area.211021', '辽阳县', NULL, '211021', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (585, '2020-03-29 14:20:36.000', 1, 'system.region.area.211081', '灯塔市', NULL, '211081', 30, NULL, 1, 16, 578, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (586, '2020-03-29 14:20:36.000', 1, 'system.region.city.211100', '盘锦市', 'area', '211100', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (587, '2020-03-29 14:20:36.000', 1, 'system.region.area.211102', '双台子区', NULL, '211102', 30, NULL, 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (588, '2020-03-29 14:20:36.000', 1, 'system.region.area.211103', '兴隆台区', NULL, '211103', 30, NULL, 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (589, '2020-03-29 14:20:36.000', 1, 'system.region.area.211104', '大洼区', NULL, '211104', 30, NULL, 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (590, '2020-03-29 14:20:36.000', 1, 'system.region.area.211122', '盘山县', NULL, '211122', 30, NULL, 1, 16, 586, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (591, '2020-03-29 14:20:36.000', 1, 'system.region.city.211200', '铁岭市', 'area', '211200', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (592, '2020-03-29 14:20:36.000', 1, 'system.region.area.211202', '银州区', NULL, '211202', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (593, '2020-03-29 14:20:36.000', 1, 'system.region.area.211204', '清河区', NULL, '211204', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (594, '2020-03-29 14:20:36.000', 1, 'system.region.area.211221', '铁岭县', NULL, '211221', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (595, '2020-03-29 14:20:36.000', 1, 'system.region.area.211223', '西丰县', NULL, '211223', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (596, '2020-03-29 14:20:36.000', 1, 'system.region.area.211224', '昌图县', NULL, '211224', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (597, '2020-03-29 14:20:36.000', 1, 'system.region.area.211281', '调兵山市', NULL, '211281', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (598, '2020-03-29 14:20:36.000', 1, 'system.region.area.211282', '开原市', NULL, '211282', 30, NULL, 1, 16, 591, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (599, '2020-03-29 14:20:36.000', 1, 'system.region.city.211300', '朝阳市', 'area', '211300', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (600, '2020-03-29 14:20:36.000', 1, 'system.region.area.211302', '双塔区', NULL, '211302', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (601, '2020-03-29 14:20:36.000', 1, 'system.region.area.211303', '龙城区', NULL, '211303', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (602, '2020-03-29 14:20:36.000', 1, 'system.region.area.211321', '朝阳县', NULL, '211321', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (603, '2020-03-29 14:20:36.000', 1, 'system.region.area.211322', '建平县', NULL, '211322', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (604, '2020-03-29 14:20:36.000', 1, 'system.region.area.211324', '喀喇沁左翼蒙古族自治县', NULL, '211324', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (605, '2020-03-29 14:20:36.000', 1, 'system.region.area.211381', '北票市', NULL, '211381', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (606, '2020-03-29 14:20:36.000', 1, 'system.region.area.211382', '凌源市', NULL, '211382', 30, NULL, 1, 16, 599, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (607, '2020-03-29 14:20:36.000', 1, 'system.region.city.211400', '葫芦岛市', 'area', '211400', 30, NULL, 1, 15, 499, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (608, '2020-03-29 14:20:36.000', 1, 'system.region.area.211402', '连山区', NULL, '211402', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (609, '2020-03-29 14:20:36.000', 1, 'system.region.area.211403', '龙港区', NULL, '211403', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (610, '2020-03-29 14:20:36.000', 1, 'system.region.area.211404', '南票区', NULL, '211404', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (611, '2020-03-29 14:20:36.000', 1, 'system.region.area.211421', '绥中县', NULL, '211421', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (612, '2020-03-29 14:20:36.000', 1, 'system.region.area.211422', '建昌县', NULL, '211422', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (613, '2020-03-29 14:20:36.000', 1, 'system.region.area.211481', '兴城市', NULL, '211481', 30, NULL, 1, 16, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (614, '2020-03-29 14:20:36.000', 1, 'system.region.province.220000', '吉林省', 'city', '220000', 30, NULL, 1, 14, 607, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (615, '2020-03-29 14:20:36.000', 1, 'system.region.city.220100', '长春市', 'area', '220100', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (616, '2020-03-29 14:20:36.000', 1, 'system.region.area.220102', '南关区', NULL, '220102', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (617, '2020-03-29 14:20:36.000', 1, 'system.region.area.220103', '宽城区', NULL, '220103', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (618, '2020-03-29 14:20:36.000', 1, 'system.region.area.220104', '朝阳区', NULL, '220104', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (619, '2020-03-29 14:20:36.000', 1, 'system.region.area.220105', '二道区', NULL, '220105', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (620, '2020-03-29 14:20:36.000', 1, 'system.region.area.220106', '绿园区', NULL, '220106', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (621, '2020-03-29 14:20:36.000', 1, 'system.region.area.220112', '双阳区', NULL, '220112', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (622, '2020-03-29 14:20:36.000', 1, 'system.region.area.220113', '九台区', NULL, '220113', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (623, '2020-03-29 14:20:36.000', 1, 'system.region.area.220122', '农安县', NULL, '220122', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (624, '2020-03-29 14:20:36.000', 1, 'system.region.area.220182', '榆树市', NULL, '220182', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (625, '2020-03-29 14:20:36.000', 1, 'system.region.area.220183', '德惠市', NULL, '220183', 30, NULL, 1, 16, 615, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (626, '2020-03-29 14:20:36.000', 1, 'system.region.city.220200', '吉林市', 'area', '220200', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (627, '2020-03-29 14:20:36.000', 1, 'system.region.area.220202', '昌邑区', NULL, '220202', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (628, '2020-03-29 14:20:36.000', 1, 'system.region.area.220203', '龙潭区', NULL, '220203', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (629, '2020-03-29 14:20:36.000', 1, 'system.region.area.220204', '船营区', NULL, '220204', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (630, '2020-03-29 14:20:36.000', 1, 'system.region.area.220211', '丰满区', NULL, '220211', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (631, '2020-03-29 14:20:36.000', 1, 'system.region.area.220221', '永吉县', NULL, '220221', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (632, '2020-03-29 14:20:36.000', 1, 'system.region.area.220281', '蛟河市', NULL, '220281', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (633, '2020-03-29 14:20:36.000', 1, 'system.region.area.220282', '桦甸市', NULL, '220282', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (634, '2020-03-29 14:20:36.000', 1, 'system.region.area.220283', '舒兰市', NULL, '220283', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (635, '2020-03-29 14:20:36.000', 1, 'system.region.area.220284', '磐石市', NULL, '220284', 30, NULL, 1, 16, 626, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (636, '2020-03-29 14:20:36.000', 1, 'system.region.city.220300', '四平市', 'area', '220300', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (637, '2020-03-29 14:20:36.000', 1, 'system.region.area.220302', '铁西区', NULL, '220302', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (638, '2020-03-29 14:20:36.000', 1, 'system.region.area.220303', '铁东区', NULL, '220303', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (639, '2020-03-29 14:20:36.000', 1, 'system.region.area.220322', '梨树县', NULL, '220322', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (640, '2020-03-29 14:20:36.000', 1, 'system.region.area.220323', '伊通满族自治县', NULL, '220323', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (641, '2020-03-29 14:20:36.000', 1, 'system.region.area.220381', '公主岭市', NULL, '220381', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (642, '2020-03-29 14:20:36.000', 1, 'system.region.area.220382', '双辽市', NULL, '220382', 30, NULL, 1, 16, 636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (643, '2020-03-29 14:20:36.000', 1, 'system.region.city.220400', '辽源市', 'area', '220400', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (644, '2020-03-29 14:20:36.000', 1, 'system.region.area.220402', '龙山区', NULL, '220402', 30, NULL, 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (645, '2020-03-29 14:20:36.000', 1, 'system.region.area.220403', '西安区', NULL, '220403', 30, NULL, 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (646, '2020-03-29 14:20:36.000', 1, 'system.region.area.220421', '东丰县', NULL, '220421', 30, NULL, 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (647, '2020-03-29 14:20:36.000', 1, 'system.region.area.220422', '东辽县', NULL, '220422', 30, NULL, 1, 16, 643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (648, '2020-03-29 14:20:36.000', 1, 'system.region.city.220500', '通化市', 'area', '220500', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (649, '2020-03-29 14:20:36.000', 1, 'system.region.area.220502', '东昌区', NULL, '220502', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (650, '2020-03-29 14:20:36.000', 1, 'system.region.area.220503', '二道江区', NULL, '220503', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (651, '2020-03-29 14:20:36.000', 1, 'system.region.area.220521', '通化县', NULL, '220521', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (652, '2020-03-29 14:20:36.000', 1, 'system.region.area.220523', '辉南县', NULL, '220523', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (653, '2020-03-29 14:20:36.000', 1, 'system.region.area.220524', '柳河县', NULL, '220524', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (654, '2020-03-29 14:20:36.000', 1, 'system.region.area.220581', '梅河口市', NULL, '220581', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (655, '2020-03-29 14:20:36.000', 1, 'system.region.area.220582', '集安市', NULL, '220582', 30, NULL, 1, 16, 648, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (656, '2020-03-29 14:20:36.000', 1, 'system.region.city.220600', '白山市', 'area', '220600', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (657, '2020-03-29 14:20:36.000', 1, 'system.region.area.220602', '浑江区', NULL, '220602', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (658, '2020-03-29 14:20:36.000', 1, 'system.region.area.220605', '江源区', NULL, '220605', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (659, '2020-03-29 14:20:36.000', 1, 'system.region.area.220621', '抚松县', NULL, '220621', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (660, '2020-03-29 14:20:36.000', 1, 'system.region.area.220622', '靖宇县', NULL, '220622', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (661, '2020-03-29 14:20:36.000', 1, 'system.region.area.220623', '长白朝鲜族自治县', NULL, '220623', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (662, '2020-03-29 14:20:36.000', 1, 'system.region.area.220681', '临江市', NULL, '220681', 30, NULL, 1, 16, 656, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (663, '2020-03-29 14:20:36.000', 1, 'system.region.city.220700', '松原市', 'area', '220700', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (664, '2020-03-29 14:20:36.000', 1, 'system.region.area.220702', '宁江区', NULL, '220702', 30, NULL, 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (665, '2020-03-29 14:20:36.000', 1, 'system.region.area.220721', '前郭尔罗斯蒙古族自治县', NULL, '220721', 30, NULL, 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (666, '2020-03-29 14:20:36.000', 1, 'system.region.area.220722', '长岭县', NULL, '220722', 30, NULL, 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (667, '2020-03-29 14:20:36.000', 1, 'system.region.area.220723', '乾安县', NULL, '220723', 30, NULL, 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (668, '2020-03-29 14:20:36.000', 1, 'system.region.area.220781', '扶余市', NULL, '220781', 30, NULL, 1, 16, 663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (669, '2020-03-29 14:20:36.000', 1, 'system.region.city.220800', '白城市', 'area', '220800', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (670, '2020-03-29 14:20:36.000', 1, 'system.region.area.220802', '洮北区', NULL, '220802', 30, NULL, 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (671, '2020-03-29 14:20:36.000', 1, 'system.region.area.220821', '镇赉县', NULL, '220821', 30, NULL, 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (672, '2020-03-29 14:20:36.000', 1, 'system.region.area.220822', '通榆县', NULL, '220822', 30, NULL, 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (673, '2020-03-29 14:20:36.000', 1, 'system.region.area.220881', '洮南市', NULL, '220881', 30, NULL, 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (674, '2020-03-29 14:20:36.000', 1, 'system.region.area.220882', '大安市', NULL, '220882', 30, NULL, 1, 16, 669, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (675, '2020-03-29 14:20:36.000', 1, 'system.region.city.222400', '延边朝鲜族自治州', 'area', '222400', 30, NULL, 1, 15, 614, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (676, '2020-03-29 14:20:36.000', 1, 'system.region.area.222401', '延吉市', NULL, '222401', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (677, '2020-03-29 14:20:36.000', 1, 'system.region.area.222402', '图们市', NULL, '222402', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (678, '2020-03-29 14:20:36.000', 1, 'system.region.area.222403', '敦化市', NULL, '222403', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (679, '2020-03-29 14:20:36.000', 1, 'system.region.area.222404', '珲春市', NULL, '222404', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (680, '2020-03-29 14:20:36.000', 1, 'system.region.area.222405', '龙井市', NULL, '222405', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (681, '2020-03-29 14:20:36.000', 1, 'system.region.area.222406', '和龙市', NULL, '222406', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (682, '2020-03-29 14:20:36.000', 1, 'system.region.area.222424', '汪清县', NULL, '222424', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (683, '2020-03-29 14:20:36.000', 1, 'system.region.area.222426', '安图县', NULL, '222426', 30, NULL, 1, 16, 675, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (684, '2020-03-29 14:20:36.000', 1, 'system.region.province.230000', '黑龙江省', 'city', '230000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (685, '2020-03-29 14:20:36.000', 1, 'system.region.city.230100', '哈尔滨市', 'area', '230100', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (686, '2020-03-29 14:20:36.000', 1, 'system.region.area.230102', '道里区', NULL, '230102', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (687, '2020-03-29 14:20:36.000', 1, 'system.region.area.230103', '南岗区', NULL, '230103', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (688, '2020-03-29 14:20:36.000', 1, 'system.region.area.230104', '道外区', NULL, '230104', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (689, '2020-03-29 14:20:36.000', 1, 'system.region.area.230108', '平房区', NULL, '230108', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (690, '2020-03-29 14:20:36.000', 1, 'system.region.area.230109', '松北区', NULL, '230109', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (691, '2020-03-29 14:20:36.000', 1, 'system.region.area.230110', '香坊区', NULL, '230110', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (692, '2020-03-29 14:20:36.000', 1, 'system.region.area.230111', '呼兰区', NULL, '230111', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (693, '2020-03-29 14:20:36.000', 1, 'system.region.area.230112', '阿城区', NULL, '230112', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (694, '2020-03-29 14:20:36.000', 1, 'system.region.area.230113', '双城区', NULL, '230113', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (695, '2020-03-29 14:20:36.000', 1, 'system.region.area.230123', '依兰县', NULL, '230123', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (696, '2020-03-29 14:20:36.000', 1, 'system.region.area.230124', '方正县', NULL, '230124', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (697, '2020-03-29 14:20:36.000', 1, 'system.region.area.230125', '宾县', NULL, '230125', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (698, '2020-03-29 14:20:36.000', 1, 'system.region.area.230126', '巴彦县', NULL, '230126', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (699, '2020-03-29 14:20:36.000', 1, 'system.region.area.230127', '木兰县', NULL, '230127', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (700, '2020-03-29 14:20:36.000', 1, 'system.region.area.230128', '通河县', NULL, '230128', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (701, '2020-03-29 14:20:36.000', 1, 'system.region.area.230129', '延寿县', NULL, '230129', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (702, '2020-03-29 14:20:36.000', 1, 'system.region.area.230183', '尚志市', NULL, '230183', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (703, '2020-03-29 14:20:36.000', 1, 'system.region.area.230184', '五常市', NULL, '230184', 30, NULL, 1, 16, 685, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (704, '2020-03-29 14:20:36.000', 1, 'system.region.city.230200', '齐齐哈尔市', 'area', '230200', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (705, '2020-03-29 14:20:36.000', 1, 'system.region.area.230202', '龙沙区', NULL, '230202', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (706, '2020-03-29 14:20:36.000', 1, 'system.region.area.230203', '建华区', NULL, '230203', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (707, '2020-03-29 14:20:36.000', 1, 'system.region.area.230204', '铁锋区', NULL, '230204', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (708, '2020-03-29 14:20:36.000', 1, 'system.region.area.230205', '昂昂溪区', NULL, '230205', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (709, '2020-03-29 14:20:36.000', 1, 'system.region.area.230206', '富拉尔基区', NULL, '230206', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (710, '2020-03-29 14:20:36.000', 1, 'system.region.area.230207', '碾子山区', NULL, '230207', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (711, '2020-03-29 14:20:36.000', 1, 'system.region.area.230208', '梅里斯达斡尔族区', NULL, '230208', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (712, '2020-03-29 14:20:36.000', 1, 'system.region.area.230221', '龙江县', NULL, '230221', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (713, '2020-03-29 14:20:36.000', 1, 'system.region.area.230223', '依安县', NULL, '230223', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (714, '2020-03-29 14:20:36.000', 1, 'system.region.area.230224', '泰来县', NULL, '230224', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (715, '2020-03-29 14:20:36.000', 1, 'system.region.area.230225', '甘南县', NULL, '230225', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (716, '2020-03-29 14:20:36.000', 1, 'system.region.area.230227', '富裕县', NULL, '230227', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (717, '2020-03-29 14:20:36.000', 1, 'system.region.area.230229', '克山县', NULL, '230229', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (718, '2020-03-29 14:20:36.000', 1, 'system.region.area.230230', '克东县', NULL, '230230', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (719, '2020-03-29 14:20:36.000', 1, 'system.region.area.230231', '拜泉县', NULL, '230231', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (720, '2020-03-29 14:20:36.000', 1, 'system.region.area.230281', '讷河市', NULL, '230281', 30, NULL, 1, 16, 704, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (721, '2020-03-29 14:20:36.000', 1, 'system.region.city.230300', '鸡西市', 'area', '230300', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (722, '2020-03-29 14:20:36.000', 1, 'system.region.area.230302', '鸡冠区', NULL, '230302', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (723, '2020-03-29 14:20:36.000', 1, 'system.region.area.230303', '恒山区', NULL, '230303', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (724, '2020-03-29 14:20:36.000', 1, 'system.region.area.230304', '滴道区', NULL, '230304', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (725, '2020-03-29 14:20:36.000', 1, 'system.region.area.230305', '梨树区', NULL, '230305', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (726, '2020-03-29 14:20:36.000', 1, 'system.region.area.230306', '城子河区', NULL, '230306', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (727, '2020-03-29 14:20:36.000', 1, 'system.region.area.230307', '麻山区', NULL, '230307', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (728, '2020-03-29 14:20:36.000', 1, 'system.region.area.230321', '鸡东县', NULL, '230321', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (729, '2020-03-29 14:20:36.000', 1, 'system.region.area.230381', '虎林市', NULL, '230381', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (730, '2020-03-29 14:20:36.000', 1, 'system.region.area.230382', '密山市', NULL, '230382', 30, NULL, 1, 16, 721, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (731, '2020-03-29 14:20:36.000', 1, 'system.region.city.230400', '鹤岗市', 'area', '230400', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (732, '2020-03-29 14:20:36.000', 1, 'system.region.area.230402', '向阳区', NULL, '230402', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (733, '2020-03-29 14:20:36.000', 1, 'system.region.area.230403', '工农区', NULL, '230403', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (734, '2020-03-29 14:20:36.000', 1, 'system.region.area.230404', '南山区', NULL, '230404', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (735, '2020-03-29 14:20:36.000', 1, 'system.region.area.230405', '兴安区', NULL, '230405', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (736, '2020-03-29 14:20:36.000', 1, 'system.region.area.230406', '东山区', NULL, '230406', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (737, '2020-03-29 14:20:36.000', 1, 'system.region.area.230407', '兴山区', NULL, '230407', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (738, '2020-03-29 14:20:36.000', 1, 'system.region.area.230421', '萝北县', NULL, '230421', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (739, '2020-03-29 14:20:36.000', 1, 'system.region.area.230422', '绥滨县', NULL, '230422', 30, NULL, 1, 16, 731, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (740, '2020-03-29 14:20:36.000', 1, 'system.region.city.230500', '双鸭山市', 'area', '230500', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (741, '2020-03-29 14:20:36.000', 1, 'system.region.area.230502', '尖山区', NULL, '230502', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (742, '2020-03-29 14:20:36.000', 1, 'system.region.area.230503', '岭东区', NULL, '230503', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (743, '2020-03-29 14:20:36.000', 1, 'system.region.area.230505', '四方台区', NULL, '230505', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (744, '2020-03-29 14:20:36.000', 1, 'system.region.area.230506', '宝山区', NULL, '230506', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (745, '2020-03-29 14:20:36.000', 1, 'system.region.area.230521', '集贤县', NULL, '230521', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (746, '2020-03-29 14:20:36.000', 1, 'system.region.area.230522', '友谊县', NULL, '230522', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (747, '2020-03-29 14:20:36.000', 1, 'system.region.area.230523', '宝清县', NULL, '230523', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (748, '2020-03-29 14:20:36.000', 1, 'system.region.area.230524', '饶河县', NULL, '230524', 30, NULL, 1, 16, 740, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (749, '2020-03-29 14:20:36.000', 1, 'system.region.city.230600', '大庆市', 'area', '230600', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (750, '2020-03-29 14:20:36.000', 1, 'system.region.area.230602', '萨尔图区', NULL, '230602', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (751, '2020-03-29 14:20:36.000', 1, 'system.region.area.230603', '龙凤区', NULL, '230603', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (752, '2020-03-29 14:20:36.000', 1, 'system.region.area.230604', '让胡路区', NULL, '230604', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (753, '2020-03-29 14:20:36.000', 1, 'system.region.area.230605', '红岗区', NULL, '230605', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (754, '2020-03-29 14:20:36.000', 1, 'system.region.area.230606', '大同区', NULL, '230606', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (755, '2020-03-29 14:20:36.000', 1, 'system.region.area.230621', '肇州县', NULL, '230621', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (756, '2020-03-29 14:20:36.000', 1, 'system.region.area.230622', '肇源县', NULL, '230622', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (757, '2020-03-29 14:20:36.000', 1, 'system.region.area.230623', '林甸县', NULL, '230623', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (758, '2020-03-29 14:20:36.000', 1, 'system.region.area.230624', '杜尔伯特蒙古族自治县', NULL, '230624', 30, NULL, 1, 16, 749, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (759, '2020-03-29 14:20:36.000', 1, 'system.region.city.230700', '伊春市', 'area', '230700', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (760, '2020-03-29 14:20:36.000', 1, 'system.region.area.230717', '伊美区', NULL, '230717', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (761, '2020-03-29 14:20:36.000', 1, 'system.region.area.230718', '乌翠区', NULL, '230718', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (762, '2020-03-29 14:20:36.000', 1, 'system.region.area.230719', '友好区', NULL, '230719', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (763, '2020-03-29 14:20:36.000', 1, 'system.region.area.230722', '嘉荫县', NULL, '230722', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (764, '2020-03-29 14:20:36.000', 1, 'system.region.area.230723', '汤旺县', NULL, '230723', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (765, '2020-03-29 14:20:36.000', 1, 'system.region.area.230724', '丰林县', NULL, '230724', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (766, '2020-03-29 14:20:36.000', 1, 'system.region.area.230725', '大箐山县', NULL, '230725', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (767, '2020-03-29 14:20:36.000', 1, 'system.region.area.230726', '南岔县', NULL, '230726', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (768, '2020-03-29 14:20:36.000', 1, 'system.region.area.230751', '金林区', NULL, '230751', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (769, '2020-03-29 14:20:36.000', 1, 'system.region.area.230781', '铁力市', NULL, '230781', 30, NULL, 1, 16, 759, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (770, '2020-03-29 14:20:36.000', 1, 'system.region.city.230800', '佳木斯市', 'area', '230800', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (771, '2020-03-29 14:20:36.000', 1, 'system.region.area.230803', '向阳区', NULL, '230803', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (772, '2020-03-29 14:20:36.000', 1, 'system.region.area.230804', '前进区', NULL, '230804', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (773, '2020-03-29 14:20:36.000', 1, 'system.region.area.230805', '东风区', NULL, '230805', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (774, '2020-03-29 14:20:36.000', 1, 'system.region.area.230811', '郊区', NULL, '230811', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (775, '2020-03-29 14:20:36.000', 1, 'system.region.area.230822', '桦南县', NULL, '230822', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (776, '2020-03-29 14:20:36.000', 1, 'system.region.area.230826', '桦川县', NULL, '230826', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (777, '2020-03-29 14:20:36.000', 1, 'system.region.area.230828', '汤原县', NULL, '230828', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (778, '2020-03-29 14:20:36.000', 1, 'system.region.area.230881', '同江市', NULL, '230881', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (779, '2020-03-29 14:20:36.000', 1, 'system.region.area.230882', '富锦市', NULL, '230882', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (780, '2020-03-29 14:20:36.000', 1, 'system.region.area.230883', '抚远市', NULL, '230883', 30, NULL, 1, 16, 770, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (781, '2020-03-29 14:20:36.000', 1, 'system.region.city.230900', '七台河市', 'area', '230900', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (782, '2020-03-29 14:20:36.000', 1, 'system.region.area.230902', '新兴区', NULL, '230902', 30, NULL, 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (783, '2020-03-29 14:20:36.000', 1, 'system.region.area.230903', '桃山区', NULL, '230903', 30, NULL, 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (784, '2020-03-29 14:20:36.000', 1, 'system.region.area.230904', '茄子河区', NULL, '230904', 30, NULL, 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (785, '2020-03-29 14:20:36.000', 1, 'system.region.area.230921', '勃利县', NULL, '230921', 30, NULL, 1, 16, 781, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (786, '2020-03-29 14:20:36.000', 1, 'system.region.city.231000', '牡丹江市', 'area', '231000', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (787, '2020-03-29 14:20:36.000', 1, 'system.region.area.231002', '东安区', NULL, '231002', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (788, '2020-03-29 14:20:36.000', 1, 'system.region.area.231003', '阳明区', NULL, '231003', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (789, '2020-03-29 14:20:36.000', 1, 'system.region.area.231004', '爱民区', NULL, '231004', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (790, '2020-03-29 14:20:36.000', 1, 'system.region.area.231005', '西安区', NULL, '231005', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (791, '2020-03-29 14:20:36.000', 1, 'system.region.area.231025', '林口县', NULL, '231025', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (792, '2020-03-29 14:20:36.000', 1, 'system.region.area.231081', '绥芬河市', NULL, '231081', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (793, '2020-03-29 14:20:36.000', 1, 'system.region.area.231083', '海林市', NULL, '231083', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (794, '2020-03-29 14:20:36.000', 1, 'system.region.area.231084', '宁安市', NULL, '231084', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (795, '2020-03-29 14:20:36.000', 1, 'system.region.area.231085', '穆棱市', NULL, '231085', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (796, '2020-03-29 14:20:36.000', 1, 'system.region.area.231086', '东宁市', NULL, '231086', 30, NULL, 1, 16, 786, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (797, '2020-03-29 14:20:36.000', 1, 'system.region.city.231100', '黑河市', 'area', '231100', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (798, '2020-03-29 14:20:36.000', 1, 'system.region.area.231102', '爱辉区', NULL, '231102', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (799, '2020-03-29 14:20:36.000', 1, 'system.region.area.231123', '逊克县', NULL, '231123', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (800, '2020-03-29 14:20:36.000', 1, 'system.region.area.231124', '孙吴县', NULL, '231124', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (801, '2020-03-29 14:20:36.000', 1, 'system.region.area.231181', '北安市', NULL, '231181', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (802, '2020-03-29 14:20:36.000', 1, 'system.region.area.231182', '五大连池市', NULL, '231182', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (803, '2020-03-29 14:20:36.000', 1, 'system.region.area.231183', '嫩江市', NULL, '231183', 30, NULL, 1, 16, 797, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (804, '2020-03-29 14:20:36.000', 1, 'system.region.city.231200', '绥化市', 'area', '231200', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (805, '2020-03-29 14:20:36.000', 1, 'system.region.area.231202', '北林区', NULL, '231202', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (806, '2020-03-29 14:20:36.000', 1, 'system.region.area.231221', '望奎县', NULL, '231221', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (807, '2020-03-29 14:20:36.000', 1, 'system.region.area.231222', '兰西县', NULL, '231222', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (808, '2020-03-29 14:20:36.000', 1, 'system.region.area.231223', '青冈县', NULL, '231223', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (809, '2020-03-29 14:20:36.000', 1, 'system.region.area.231224', '庆安县', NULL, '231224', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (810, '2020-03-29 14:20:36.000', 1, 'system.region.area.231225', '明水县', NULL, '231225', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (811, '2020-03-29 14:20:36.000', 1, 'system.region.area.231226', '绥棱县', NULL, '231226', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (812, '2020-03-29 14:20:36.000', 1, 'system.region.area.231281', '安达市', NULL, '231281', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (813, '2020-03-29 14:20:36.000', 1, 'system.region.area.231282', '肇东市', NULL, '231282', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (814, '2020-03-29 14:20:36.000', 1, 'system.region.area.231283', '海伦市', NULL, '231283', 30, NULL, 1, 16, 804, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (815, '2020-03-29 14:20:36.000', 1, 'system.region.city.232700', '大兴安岭地区', 'area', '232700', 30, NULL, 1, 15, 684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (816, '2020-03-29 14:20:36.000', 1, 'system.region.area.232701', '漠河市', NULL, '232701', 30, NULL, 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (817, '2020-03-29 14:20:36.000', 1, 'system.region.area.232721', '呼玛县', NULL, '232721', 30, NULL, 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (818, '2020-03-29 14:20:36.000', 1, 'system.region.area.232722', '塔河县', NULL, '232722', 30, NULL, 1, 16, 815, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (819, '2020-03-29 14:20:36.000', 1, 'system.region.province.310000', '上海市', 'area', '310000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (820, '2020-03-29 14:20:36.000', 1, 'system.region.area.310101', '黄浦区', NULL, '310101', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (821, '2020-03-29 14:20:36.000', 1, 'system.region.area.310104', '徐汇区', NULL, '310104', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (822, '2020-03-29 14:20:36.000', 1, 'system.region.area.310105', '长宁区', NULL, '310105', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (823, '2020-03-29 14:20:36.000', 1, 'system.region.area.310106', '静安区', NULL, '310106', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (824, '2020-03-29 14:20:36.000', 1, 'system.region.area.310107', '普陀区', NULL, '310107', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (825, '2020-03-29 14:20:36.000', 1, 'system.region.area.310109', '虹口区', NULL, '310109', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (826, '2020-03-29 14:20:36.000', 1, 'system.region.area.310110', '杨浦区', NULL, '310110', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (827, '2020-03-29 14:20:36.000', 1, 'system.region.area.310112', '闵行区', NULL, '310112', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (828, '2020-03-29 14:20:36.000', 1, 'system.region.area.310113', '宝山区', NULL, '310113', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (829, '2020-03-29 14:20:36.000', 1, 'system.region.area.310114', '嘉定区', NULL, '310114', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (830, '2020-03-29 14:20:36.000', 1, 'system.region.area.310115', '浦东新区', NULL, '310115', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (831, '2020-03-29 14:20:36.000', 1, 'system.region.area.310116', '金山区', NULL, '310116', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (832, '2020-03-29 14:20:36.000', 1, 'system.region.area.310117', '松江区', NULL, '310117', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (833, '2020-03-29 14:20:36.000', 1, 'system.region.area.310118', '青浦区', NULL, '310118', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (834, '2020-03-29 14:20:36.000', 1, 'system.region.area.310120', '奉贤区', NULL, '310120', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (835, '2020-03-29 14:20:36.000', 1, 'system.region.area.310151', '崇明区', NULL, '310151', 30, NULL, 1, 16, 819, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (836, '2020-03-29 14:20:36.000', 1, 'system.region.province.320000', '江苏省', 'city', '320000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (837, '2020-03-29 14:20:36.000', 1, 'system.region.city.320100', '南京市', 'area', '320100', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (838, '2020-03-29 14:20:36.000', 1, 'system.region.area.320102', '玄武区', NULL, '320102', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (839, '2020-03-29 14:20:36.000', 1, 'system.region.area.320104', '秦淮区', NULL, '320104', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (840, '2020-03-29 14:20:36.000', 1, 'system.region.area.320105', '建邺区', NULL, '320105', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (841, '2020-03-29 14:20:36.000', 1, 'system.region.area.320106', '鼓楼区', NULL, '320106', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (842, '2020-03-29 14:20:36.000', 1, 'system.region.area.320111', '浦口区', NULL, '320111', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (843, '2020-03-29 14:20:36.000', 1, 'system.region.area.320113', '栖霞区', NULL, '320113', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (844, '2020-03-29 14:20:36.000', 1, 'system.region.area.320114', '雨花台区', NULL, '320114', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (845, '2020-03-29 14:20:36.000', 1, 'system.region.area.320115', '江宁区', NULL, '320115', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (846, '2020-03-29 14:20:36.000', 1, 'system.region.area.320116', '六合区', NULL, '320116', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (847, '2020-03-29 14:20:36.000', 1, 'system.region.area.320117', '溧水区', NULL, '320117', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (848, '2020-03-29 14:20:36.000', 1, 'system.region.area.320118', '高淳区', NULL, '320118', 30, NULL, 1, 16, 837, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (849, '2020-03-29 14:20:36.000', 1, 'system.region.city.320200', '无锡市', 'area', '320200', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (850, '2020-03-29 14:20:36.000', 1, 'system.region.area.320205', '锡山区', NULL, '320205', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (851, '2020-03-29 14:20:36.000', 1, 'system.region.area.320206', '惠山区', NULL, '320206', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (852, '2020-03-29 14:20:36.000', 1, 'system.region.area.320211', '滨湖区', NULL, '320211', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (853, '2020-03-29 14:20:36.000', 1, 'system.region.area.320213', '梁溪区', NULL, '320213', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (854, '2020-03-29 14:20:36.000', 1, 'system.region.area.320214', '新吴区', NULL, '320214', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (855, '2020-03-29 14:20:36.000', 1, 'system.region.area.320281', '江阴市', NULL, '320281', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (856, '2020-03-29 14:20:36.000', 1, 'system.region.area.320282', '宜兴市', NULL, '320282', 30, NULL, 1, 16, 849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (857, '2020-03-29 14:20:36.000', 1, 'system.region.city.320300', '徐州市', 'area', '320300', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (858, '2020-03-29 14:20:36.000', 1, 'system.region.area.320302', '鼓楼区', NULL, '320302', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (859, '2020-03-29 14:20:36.000', 1, 'system.region.area.320303', '云龙区', NULL, '320303', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (860, '2020-03-29 14:20:36.000', 1, 'system.region.area.320305', '贾汪区', NULL, '320305', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (861, '2020-03-29 14:20:36.000', 1, 'system.region.area.320311', '泉山区', NULL, '320311', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (862, '2020-03-29 14:20:36.000', 1, 'system.region.area.320312', '铜山区', NULL, '320312', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (863, '2020-03-29 14:20:36.000', 1, 'system.region.area.320321', '丰县', NULL, '320321', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (864, '2020-03-29 14:20:36.000', 1, 'system.region.area.320322', '沛县', NULL, '320322', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (865, '2020-03-29 14:20:36.000', 1, 'system.region.area.320324', '睢宁县', NULL, '320324', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (866, '2020-03-29 14:20:36.000', 1, 'system.region.area.320381', '新沂市', NULL, '320381', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (867, '2020-03-29 14:20:36.000', 1, 'system.region.area.320382', '邳州市', NULL, '320382', 30, NULL, 1, 16, 857, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (868, '2020-03-29 14:20:36.000', 1, 'system.region.city.320400', '常州市', 'area', '320400', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (869, '2020-03-29 14:20:36.000', 1, 'system.region.area.320402', '天宁区', NULL, '320402', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (870, '2020-03-29 14:20:36.000', 1, 'system.region.area.320404', '钟楼区', NULL, '320404', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (871, '2020-03-29 14:20:36.000', 1, 'system.region.area.320411', '新北区', NULL, '320411', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (872, '2020-03-29 14:20:36.000', 1, 'system.region.area.320412', '武进区', NULL, '320412', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (873, '2020-03-29 14:20:36.000', 1, 'system.region.area.320413', '金坛区', NULL, '320413', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (874, '2020-03-29 14:20:36.000', 1, 'system.region.area.320481', '溧阳市', NULL, '320481', 30, NULL, 1, 16, 868, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (875, '2020-03-29 14:20:36.000', 1, 'system.region.city.320500', '苏州市', 'area', '320500', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (876, '2020-03-29 14:20:36.000', 1, 'system.region.area.320505', '虎丘区', NULL, '320505', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (877, '2020-03-29 14:20:36.000', 1, 'system.region.area.320506', '吴中区', NULL, '320506', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (878, '2020-03-29 14:20:36.000', 1, 'system.region.area.320507', '相城区', NULL, '320507', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (879, '2020-03-29 14:20:36.000', 1, 'system.region.area.320508', '姑苏区', NULL, '320508', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (880, '2020-03-29 14:20:36.000', 1, 'system.region.area.320509', '吴江区', NULL, '320509', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (881, '2020-03-29 14:20:36.000', 1, 'system.region.area.320581', '常熟市', NULL, '320581', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (882, '2020-03-29 14:20:36.000', 1, 'system.region.area.320582', '张家港市', NULL, '320582', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (883, '2020-03-29 14:20:36.000', 1, 'system.region.area.320583', '昆山市', NULL, '320583', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (884, '2020-03-29 14:20:36.000', 1, 'system.region.area.320585', '太仓市', NULL, '320585', 30, NULL, 1, 16, 875, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (885, '2020-03-29 14:20:36.000', 1, 'system.region.city.320600', '南通市', 'area', '320600', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (886, '2020-03-29 14:20:36.000', 1, 'system.region.area.320602', '崇川区', NULL, '320602', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (887, '2020-03-29 14:20:36.000', 1, 'system.region.area.320611', '港闸区', NULL, '320611', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (888, '2020-03-29 14:20:36.000', 1, 'system.region.area.320612', '通州区', NULL, '320612', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (889, '2020-03-29 14:20:36.000', 1, 'system.region.area.320623', '如东县', NULL, '320623', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (890, '2020-03-29 14:20:36.000', 1, 'system.region.area.320681', '启东市', NULL, '320681', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (891, '2020-03-29 14:20:36.000', 1, 'system.region.area.320682', '如皋市', NULL, '320682', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (892, '2020-03-29 14:20:36.000', 1, 'system.region.area.320684', '海门市', NULL, '320684', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (893, '2020-03-29 14:20:36.000', 1, 'system.region.area.320685', '海安市', NULL, '320685', 30, NULL, 1, 16, 885, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (894, '2020-03-29 14:20:36.000', 1, 'system.region.city.320700', '连云港市', 'area', '320700', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (895, '2020-03-29 14:20:36.000', 1, 'system.region.area.320703', '连云区', NULL, '320703', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (896, '2020-03-29 14:20:36.000', 1, 'system.region.area.320706', '海州区', NULL, '320706', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (897, '2020-03-29 14:20:36.000', 1, 'system.region.area.320707', '赣榆区', NULL, '320707', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (898, '2020-03-29 14:20:36.000', 1, 'system.region.area.320722', '东海县', NULL, '320722', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (899, '2020-03-29 14:20:36.000', 1, 'system.region.area.320723', '灌云县', NULL, '320723', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (900, '2020-03-29 14:20:36.000', 1, 'system.region.area.320724', '灌南县', NULL, '320724', 30, NULL, 1, 16, 894, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (901, '2020-03-29 14:20:36.000', 1, 'system.region.city.320800', '淮安市', 'area', '320800', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (902, '2020-03-29 14:20:36.000', 1, 'system.region.area.320803', '淮安区', NULL, '320803', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (903, '2020-03-29 14:20:36.000', 1, 'system.region.area.320804', '淮阴区', NULL, '320804', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (904, '2020-03-29 14:20:36.000', 1, 'system.region.area.320812', '清江浦区', NULL, '320812', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (905, '2020-03-29 14:20:36.000', 1, 'system.region.area.320813', '洪泽区', NULL, '320813', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (906, '2020-03-29 14:20:36.000', 1, 'system.region.area.320826', '涟水县', NULL, '320826', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (907, '2020-03-29 14:20:36.000', 1, 'system.region.area.320830', '盱眙县', NULL, '320830', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (908, '2020-03-29 14:20:36.000', 1, 'system.region.area.320831', '金湖县', NULL, '320831', 30, NULL, 1, 16, 901, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (909, '2020-03-29 14:20:36.000', 1, 'system.region.city.320900', '盐城市', 'area', '320900', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (910, '2020-03-29 14:20:36.000', 1, 'system.region.area.320902', '亭湖区', NULL, '320902', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (911, '2020-03-29 14:20:36.000', 1, 'system.region.area.320903', '盐都区', NULL, '320903', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (912, '2020-03-29 14:20:36.000', 1, 'system.region.area.320904', '大丰区', NULL, '320904', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (913, '2020-03-29 14:20:36.000', 1, 'system.region.area.320921', '响水县', NULL, '320921', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (914, '2020-03-29 14:20:36.000', 1, 'system.region.area.320922', '滨海县', NULL, '320922', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (915, '2020-03-29 14:20:36.000', 1, 'system.region.area.320923', '阜宁县', NULL, '320923', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (916, '2020-03-29 14:20:36.000', 1, 'system.region.area.320924', '射阳县', NULL, '320924', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (917, '2020-03-29 14:20:36.000', 1, 'system.region.area.320925', '建湖县', NULL, '320925', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (918, '2020-03-29 14:20:36.000', 1, 'system.region.area.320981', '东台市', NULL, '320981', 30, NULL, 1, 16, 909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (919, '2020-03-29 14:20:36.000', 1, 'system.region.city.321000', '扬州市', 'area', '321000', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (920, '2020-03-29 14:20:36.000', 1, 'system.region.area.321002', '广陵区', NULL, '321002', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (921, '2020-03-29 14:20:36.000', 1, 'system.region.area.321003', '邗江区', NULL, '321003', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (922, '2020-03-29 14:20:36.000', 1, 'system.region.area.321012', '江都区', NULL, '321012', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (923, '2020-03-29 14:20:36.000', 1, 'system.region.area.321023', '宝应县', NULL, '321023', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (924, '2020-03-29 14:20:36.000', 1, 'system.region.area.321081', '仪征市', NULL, '321081', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (925, '2020-03-29 14:20:36.000', 1, 'system.region.area.321084', '高邮市', NULL, '321084', 30, NULL, 1, 16, 919, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (926, '2020-03-29 14:20:36.000', 1, 'system.region.city.321100', '镇江市', 'area', '321100', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (927, '2020-03-29 14:20:36.000', 1, 'system.region.area.321102', '京口区', NULL, '321102', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (928, '2020-03-29 14:20:36.000', 1, 'system.region.area.321111', '润州区', NULL, '321111', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (929, '2020-03-29 14:20:36.000', 1, 'system.region.area.321112', '丹徒区', NULL, '321112', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (930, '2020-03-29 14:20:36.000', 1, 'system.region.area.321181', '丹阳市', NULL, '321181', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (931, '2020-03-29 14:20:36.000', 1, 'system.region.area.321182', '扬中市', NULL, '321182', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (932, '2020-03-29 14:20:36.000', 1, 'system.region.area.321183', '句容市', NULL, '321183', 30, NULL, 1, 16, 926, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (933, '2020-03-29 14:20:36.000', 1, 'system.region.city.321200', '泰州市', 'area', '321200', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (934, '2020-03-29 14:20:36.000', 1, 'system.region.area.321202', '海陵区', NULL, '321202', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (935, '2020-03-29 14:20:36.000', 1, 'system.region.area.321203', '高港区', NULL, '321203', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (936, '2020-03-29 14:20:36.000', 1, 'system.region.area.321204', '姜堰区', NULL, '321204', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (937, '2020-03-29 14:20:36.000', 1, 'system.region.area.321281', '兴化市', NULL, '321281', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (938, '2020-03-29 14:20:36.000', 1, 'system.region.area.321282', '靖江市', NULL, '321282', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (939, '2020-03-29 14:20:36.000', 1, 'system.region.area.321283', '泰兴市', NULL, '321283', 30, NULL, 1, 16, 933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (940, '2020-03-29 14:20:36.000', 1, 'system.region.city.321300', '宿迁市', 'area', '321300', 30, NULL, 1, 15, 836, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (941, '2020-03-29 14:20:36.000', 1, 'system.region.area.321302', '宿城区', NULL, '321302', 30, NULL, 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (942, '2020-03-29 14:20:36.000', 1, 'system.region.area.321311', '宿豫区', NULL, '321311', 30, NULL, 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (943, '2020-03-29 14:20:36.000', 1, 'system.region.area.321322', '沭阳县', NULL, '321322', 30, NULL, 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (944, '2020-03-29 14:20:36.000', 1, 'system.region.area.321323', '泗阳县', NULL, '321323', 30, NULL, 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (945, '2020-03-29 14:20:36.000', 1, 'system.region.area.321324', '泗洪县', NULL, '321324', 30, NULL, 1, 16, 940, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (946, '2020-03-29 14:20:36.000', 1, 'system.region.province.330000', '浙江省', 'city', '330000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (947, '2020-03-29 14:20:36.000', 1, 'system.region.city.330100', '杭州市', 'area', '330100', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (948, '2020-03-29 14:20:36.000', 1, 'system.region.area.330102', '上城区', NULL, '330102', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (949, '2020-03-29 14:20:36.000', 1, 'system.region.area.330103', '下城区', NULL, '330103', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (950, '2020-03-29 14:20:36.000', 1, 'system.region.area.330104', '江干区', NULL, '330104', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (951, '2020-03-29 14:20:36.000', 1, 'system.region.area.330105', '拱墅区', NULL, '330105', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (952, '2020-03-29 14:20:36.000', 1, 'system.region.area.330106', '西湖区', NULL, '330106', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (953, '2020-03-29 14:20:36.000', 1, 'system.region.area.330108', '滨江区', NULL, '330108', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (954, '2020-03-29 14:20:36.000', 1, 'system.region.area.330109', '萧山区', NULL, '330109', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (955, '2020-03-29 14:20:36.000', 1, 'system.region.area.330110', '余杭区', NULL, '330110', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (956, '2020-03-29 14:20:36.000', 1, 'system.region.area.330111', '富阳区', NULL, '330111', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (957, '2020-03-29 14:20:36.000', 1, 'system.region.area.330112', '临安区', NULL, '330112', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (958, '2020-03-29 14:20:36.000', 1, 'system.region.area.330122', '桐庐县', NULL, '330122', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (959, '2020-03-29 14:20:36.000', 1, 'system.region.area.330127', '淳安县', NULL, '330127', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (960, '2020-03-29 14:20:36.000', 1, 'system.region.area.330182', '建德市', NULL, '330182', 30, NULL, 1, 16, 947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (961, '2020-03-29 14:20:36.000', 1, 'system.region.city.330200', '宁波市', 'area', '330200', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (962, '2020-03-29 14:20:36.000', 1, 'system.region.area.330203', '海曙区', NULL, '330203', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (963, '2020-03-29 14:20:36.000', 1, 'system.region.area.330205', '江北区', NULL, '330205', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (964, '2020-03-29 14:20:36.000', 1, 'system.region.area.330206', '北仑区', NULL, '330206', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (965, '2020-03-29 14:20:36.000', 1, 'system.region.area.330211', '镇海区', NULL, '330211', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (966, '2020-03-29 14:20:36.000', 1, 'system.region.area.330212', '鄞州区', NULL, '330212', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (967, '2020-03-29 14:20:36.000', 1, 'system.region.area.330213', '奉化区', NULL, '330213', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (968, '2020-03-29 14:20:36.000', 1, 'system.region.area.330225', '象山县', NULL, '330225', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (969, '2020-03-29 14:20:36.000', 1, 'system.region.area.330226', '宁海县', NULL, '330226', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (970, '2020-03-29 14:20:36.000', 1, 'system.region.area.330281', '余姚市', NULL, '330281', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (971, '2020-03-29 14:20:36.000', 1, 'system.region.area.330282', '慈溪市', NULL, '330282', 30, NULL, 1, 16, 961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (972, '2020-03-29 14:20:36.000', 1, 'system.region.city.330300', '温州市', 'area', '330300', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (973, '2020-03-29 14:20:36.000', 1, 'system.region.area.330302', '鹿城区', NULL, '330302', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (974, '2020-03-29 14:20:36.000', 1, 'system.region.area.330303', '龙湾区', NULL, '330303', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (975, '2020-03-29 14:20:36.000', 1, 'system.region.area.330304', '瓯海区', NULL, '330304', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (976, '2020-03-29 14:20:36.000', 1, 'system.region.area.330305', '洞头区', NULL, '330305', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (977, '2020-03-29 14:20:36.000', 1, 'system.region.area.330324', '永嘉县', NULL, '330324', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (978, '2020-03-29 14:20:36.000', 1, 'system.region.area.330326', '平阳县', NULL, '330326', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (979, '2020-03-29 14:20:36.000', 1, 'system.region.area.330327', '苍南县', NULL, '330327', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (980, '2020-03-29 14:20:36.000', 1, 'system.region.area.330328', '文成县', NULL, '330328', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (981, '2020-03-29 14:20:36.000', 1, 'system.region.area.330329', '泰顺县', NULL, '330329', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (982, '2020-03-29 14:20:36.000', 1, 'system.region.area.330381', '瑞安市', NULL, '330381', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (983, '2020-03-29 14:20:36.000', 1, 'system.region.area.330382', '乐清市', NULL, '330382', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (984, '2020-03-29 14:20:36.000', 1, 'system.region.area.330383', '龙港市', NULL, '330383', 30, NULL, 1, 16, 972, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (985, '2020-03-29 14:20:36.000', 1, 'system.region.city.330400', '嘉兴市', 'area', '330400', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (986, '2020-03-29 14:20:36.000', 1, 'system.region.area.330402', '南湖区', NULL, '330402', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (987, '2020-03-29 14:20:36.000', 1, 'system.region.area.330411', '秀洲区', NULL, '330411', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (988, '2020-03-29 14:20:36.000', 1, 'system.region.area.330421', '嘉善县', NULL, '330421', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (989, '2020-03-29 14:20:36.000', 1, 'system.region.area.330424', '海盐县', NULL, '330424', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (990, '2020-03-29 14:20:36.000', 1, 'system.region.area.330481', '海宁市', NULL, '330481', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (991, '2020-03-29 14:20:36.000', 1, 'system.region.area.330482', '平湖市', NULL, '330482', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (992, '2020-03-29 14:20:36.000', 1, 'system.region.area.330483', '桐乡市', NULL, '330483', 30, NULL, 1, 16, 985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (993, '2020-03-29 14:20:36.000', 1, 'system.region.city.330500', '湖州市', 'area', '330500', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (994, '2020-03-29 14:20:36.000', 1, 'system.region.area.330502', '吴兴区', NULL, '330502', 30, NULL, 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (995, '2020-03-29 14:20:36.000', 1, 'system.region.area.330503', '南浔区', NULL, '330503', 30, NULL, 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (996, '2020-03-29 14:20:36.000', 1, 'system.region.area.330521', '德清县', NULL, '330521', 30, NULL, 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (997, '2020-03-29 14:20:36.000', 1, 'system.region.area.330522', '长兴县', NULL, '330522', 30, NULL, 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (998, '2020-03-29 14:20:36.000', 1, 'system.region.area.330523', '安吉县', NULL, '330523', 30, NULL, 1, 16, 993, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (999, '2020-03-29 14:20:36.000', 1, 'system.region.city.330600', '绍兴市', 'area', '330600', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1000, '2020-03-29 14:20:36.000', 1, 'system.region.area.330602', '越城区', NULL, '330602', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1001, '2020-03-29 14:20:36.000', 1, 'system.region.area.330603', '柯桥区', NULL, '330603', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1002, '2020-03-29 14:20:36.000', 1, 'system.region.area.330604', '上虞区', NULL, '330604', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1003, '2020-03-29 14:20:36.000', 1, 'system.region.area.330624', '新昌县', NULL, '330624', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1004, '2020-03-29 14:20:36.000', 1, 'system.region.area.330681', '诸暨市', NULL, '330681', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1005, '2020-03-29 14:20:36.000', 1, 'system.region.area.330683', '嵊州市', NULL, '330683', 30, NULL, 1, 16, 999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1006, '2020-03-29 14:20:36.000', 1, 'system.region.city.330700', '金华市', 'area', '330700', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1007, '2020-03-29 14:20:36.000', 1, 'system.region.area.330702', '婺城区', NULL, '330702', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1008, '2020-03-29 14:20:36.000', 1, 'system.region.area.330703', '金东区', NULL, '330703', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1009, '2020-03-29 14:20:36.000', 1, 'system.region.area.330723', '武义县', NULL, '330723', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1010, '2020-03-29 14:20:36.000', 1, 'system.region.area.330726', '浦江县', NULL, '330726', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1011, '2020-03-29 14:20:36.000', 1, 'system.region.area.330727', '磐安县', NULL, '330727', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1012, '2020-03-29 14:20:36.000', 1, 'system.region.area.330781', '兰溪市', NULL, '330781', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1013, '2020-03-29 14:20:36.000', 1, 'system.region.area.330782', '义乌市', NULL, '330782', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1014, '2020-03-29 14:20:36.000', 1, 'system.region.area.330783', '东阳市', NULL, '330783', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1015, '2020-03-29 14:20:36.000', 1, 'system.region.area.330784', '永康市', NULL, '330784', 30, NULL, 1, 16, 1006, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1016, '2020-03-29 14:20:36.000', 1, 'system.region.city.330800', '衢州市', 'area', '330800', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1017, '2020-03-29 14:20:36.000', 1, 'system.region.area.330802', '柯城区', NULL, '330802', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1018, '2020-03-29 14:20:36.000', 1, 'system.region.area.330803', '衢江区', NULL, '330803', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1019, '2020-03-29 14:20:36.000', 1, 'system.region.area.330822', '常山县', NULL, '330822', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1020, '2020-03-29 14:20:36.000', 1, 'system.region.area.330824', '开化县', NULL, '330824', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1021, '2020-03-29 14:20:36.000', 1, 'system.region.area.330825', '龙游县', NULL, '330825', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1022, '2020-03-29 14:20:36.000', 1, 'system.region.area.330881', '江山市', NULL, '330881', 30, NULL, 1, 16, 1016, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1023, '2020-03-29 14:20:36.000', 1, 'system.region.city.330900', '舟山市', 'area', '330900', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1024, '2020-03-29 14:20:36.000', 1, 'system.region.area.330902', '定海区', NULL, '330902', 30, NULL, 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1025, '2020-03-29 14:20:36.000', 1, 'system.region.area.330903', '普陀区', NULL, '330903', 30, NULL, 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1026, '2020-03-29 14:20:36.000', 1, 'system.region.area.330921', '岱山县', NULL, '330921', 30, NULL, 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1027, '2020-03-29 14:20:36.000', 1, 'system.region.area.330922', '嵊泗县', NULL, '330922', 30, NULL, 1, 16, 1023, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1028, '2020-03-29 14:20:36.000', 1, 'system.region.city.331000', '台州市', 'area', '331000', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1029, '2020-03-29 14:20:36.000', 1, 'system.region.area.331002', '椒江区', NULL, '331002', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1030, '2020-03-29 14:20:36.000', 1, 'system.region.area.331003', '黄岩区', NULL, '331003', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1031, '2020-03-29 14:20:36.000', 1, 'system.region.area.331004', '路桥区', NULL, '331004', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1032, '2020-03-29 14:20:36.000', 1, 'system.region.area.331022', '三门县', NULL, '331022', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1033, '2020-03-29 14:20:36.000', 1, 'system.region.area.331023', '天台县', NULL, '331023', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1034, '2020-03-29 14:20:36.000', 1, 'system.region.area.331024', '仙居县', NULL, '331024', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1035, '2020-03-29 14:20:36.000', 1, 'system.region.area.331081', '温岭市', NULL, '331081', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1036, '2020-03-29 14:20:36.000', 1, 'system.region.area.331082', '临海市', NULL, '331082', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1037, '2020-03-29 14:20:36.000', 1, 'system.region.area.331083', '玉环市', NULL, '331083', 30, NULL, 1, 16, 1028, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1038, '2020-03-29 14:20:36.000', 1, 'system.region.city.331100', '丽水市', 'area', '331100', 30, NULL, 1, 15, 946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1039, '2020-03-29 14:20:36.000', 1, 'system.region.area.331102', '莲都区', NULL, '331102', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1040, '2020-03-29 14:20:36.000', 1, 'system.region.area.331121', '青田县', NULL, '331121', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1041, '2020-03-29 14:20:36.000', 1, 'system.region.area.331122', '缙云县', NULL, '331122', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1042, '2020-03-29 14:20:36.000', 1, 'system.region.area.331123', '遂昌县', NULL, '331123', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1043, '2020-03-29 14:20:36.000', 1, 'system.region.area.331124', '松阳县', NULL, '331124', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1044, '2020-03-29 14:20:36.000', 1, 'system.region.area.331125', '云和县', NULL, '331125', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1045, '2020-03-29 14:20:36.000', 1, 'system.region.area.331126', '庆元县', NULL, '331126', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1046, '2020-03-29 14:20:36.000', 1, 'system.region.area.331127', '景宁畲族自治县', NULL, '331127', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1047, '2020-03-29 14:20:36.000', 1, 'system.region.area.331181', '龙泉市', NULL, '331181', 30, NULL, 1, 16, 1038, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1048, '2020-03-29 14:20:36.000', 1, 'system.region.province.340000', '安徽省', 'city', '340000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1049, '2020-03-29 14:20:36.000', 1, 'system.region.city.340100', '合肥市', 'area', '340100', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1050, '2020-03-29 14:20:36.000', 1, 'system.region.area.340102', '瑶海区', NULL, '340102', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1051, '2020-03-29 14:20:36.000', 1, 'system.region.area.340103', '庐阳区', NULL, '340103', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1052, '2020-03-29 14:20:36.000', 1, 'system.region.area.340104', '蜀山区', NULL, '340104', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1053, '2020-03-29 14:20:36.000', 1, 'system.region.area.340111', '包河区', NULL, '340111', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1054, '2020-03-29 14:20:36.000', 1, 'system.region.area.340121', '长丰县', NULL, '340121', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1055, '2020-03-29 14:20:36.000', 1, 'system.region.area.340122', '肥东县', NULL, '340122', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1056, '2020-03-29 14:20:36.000', 1, 'system.region.area.340123', '肥西县', NULL, '340123', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1057, '2020-03-29 14:20:36.000', 1, 'system.region.area.340124', '庐江县', NULL, '340124', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1058, '2020-03-29 14:20:36.000', 1, 'system.region.area.340181', '巢湖市', NULL, '340181', 30, NULL, 1, 16, 1049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1059, '2020-03-29 14:20:36.000', 1, 'system.region.city.340200', '芜湖市', 'area', '340200', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1060, '2020-03-29 14:20:36.000', 1, 'system.region.area.340202', '镜湖区', NULL, '340202', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1061, '2020-03-29 14:20:36.000', 1, 'system.region.area.340203', '弋江区', NULL, '340203', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1062, '2020-03-29 14:20:36.000', 1, 'system.region.area.340207', '鸠江区', NULL, '340207', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1063, '2020-03-29 14:20:36.000', 1, 'system.region.area.340208', '三山区', NULL, '340208', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1064, '2020-03-29 14:20:36.000', 1, 'system.region.area.340221', '芜湖县', NULL, '340221', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1065, '2020-03-29 14:20:36.000', 1, 'system.region.area.340222', '繁昌县', NULL, '340222', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1066, '2020-03-29 14:20:36.000', 1, 'system.region.area.340223', '南陵县', NULL, '340223', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1067, '2020-03-29 14:20:36.000', 1, 'system.region.area.340281', '无为市', NULL, '340281', 30, NULL, 1, 16, 1059, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1068, '2020-03-29 14:20:36.000', 1, 'system.region.city.340300', '蚌埠市', 'area', '340300', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1069, '2020-03-29 14:20:36.000', 1, 'system.region.area.340302', '龙子湖区', NULL, '340302', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1070, '2020-03-29 14:20:36.000', 1, 'system.region.area.340303', '蚌山区', NULL, '340303', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1071, '2020-03-29 14:20:36.000', 1, 'system.region.area.340304', '禹会区', NULL, '340304', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1072, '2020-03-29 14:20:36.000', 1, 'system.region.area.340311', '淮上区', NULL, '340311', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1073, '2020-03-29 14:20:36.000', 1, 'system.region.area.340321', '怀远县', NULL, '340321', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1074, '2020-03-29 14:20:36.000', 1, 'system.region.area.340322', '五河县', NULL, '340322', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1075, '2020-03-29 14:20:36.000', 1, 'system.region.area.340323', '固镇县', NULL, '340323', 30, NULL, 1, 16, 1068, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1076, '2020-03-29 14:20:36.000', 1, 'system.region.city.340400', '淮南市', 'area', '340400', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1077, '2020-03-29 14:20:36.000', 1, 'system.region.area.340402', '大通区', NULL, '340402', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1078, '2020-03-29 14:20:36.000', 1, 'system.region.area.340403', '田家庵区', NULL, '340403', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1079, '2020-03-29 14:20:36.000', 1, 'system.region.area.340404', '谢家集区', NULL, '340404', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1080, '2020-03-29 14:20:36.000', 1, 'system.region.area.340405', '八公山区', NULL, '340405', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1081, '2020-03-29 14:20:36.000', 1, 'system.region.area.340406', '潘集区', NULL, '340406', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1082, '2020-03-29 14:20:36.000', 1, 'system.region.area.340421', '凤台县', NULL, '340421', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1083, '2020-03-29 14:20:36.000', 1, 'system.region.area.340422', '寿县', NULL, '340422', 30, NULL, 1, 16, 1076, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1084, '2020-03-29 14:20:36.000', 1, 'system.region.city.340500', '马鞍山市', 'area', '340500', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1085, '2020-03-29 14:20:36.000', 1, 'system.region.area.340503', '花山区', NULL, '340503', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1086, '2020-03-29 14:20:36.000', 1, 'system.region.area.340504', '雨山区', NULL, '340504', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1087, '2020-03-29 14:20:36.000', 1, 'system.region.area.340506', '博望区', NULL, '340506', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1088, '2020-03-29 14:20:36.000', 1, 'system.region.area.340521', '当涂县', NULL, '340521', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1089, '2020-03-29 14:20:36.000', 1, 'system.region.area.340522', '含山县', NULL, '340522', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1090, '2020-03-29 14:20:36.000', 1, 'system.region.area.340523', '和县', NULL, '340523', 30, NULL, 1, 16, 1084, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1091, '2020-03-29 14:20:36.000', 1, 'system.region.city.340600', '淮北市', 'area', '340600', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1092, '2020-03-29 14:20:36.000', 1, 'system.region.area.340602', '杜集区', NULL, '340602', 30, NULL, 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1093, '2020-03-29 14:20:36.000', 1, 'system.region.area.340603', '相山区', NULL, '340603', 30, NULL, 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1094, '2020-03-29 14:20:36.000', 1, 'system.region.area.340604', '烈山区', NULL, '340604', 30, NULL, 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1095, '2020-03-29 14:20:36.000', 1, 'system.region.area.340621', '濉溪县', NULL, '340621', 30, NULL, 1, 16, 1091, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1096, '2020-03-29 14:20:36.000', 1, 'system.region.city.340700', '铜陵市', 'area', '340700', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1097, '2020-03-29 14:20:36.000', 1, 'system.region.area.340705', '铜官区', NULL, '340705', 30, NULL, 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1098, '2020-03-29 14:20:36.000', 1, 'system.region.area.340706', '义安区', NULL, '340706', 30, NULL, 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1099, '2020-03-29 14:20:36.000', 1, 'system.region.area.340711', '郊区', NULL, '340711', 30, NULL, 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1100, '2020-03-29 14:20:36.000', 1, 'system.region.area.340722', '枞阳县', NULL, '340722', 30, NULL, 1, 16, 1096, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1101, '2020-03-29 14:20:36.000', 1, 'system.region.city.340800', '安庆市', 'area', '340800', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1102, '2020-03-29 14:20:36.000', 1, 'system.region.area.340802', '迎江区', NULL, '340802', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1103, '2020-03-29 14:20:36.000', 1, 'system.region.area.340803', '大观区', NULL, '340803', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1104, '2020-03-29 14:20:36.000', 1, 'system.region.area.340811', '宜秀区', NULL, '340811', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1105, '2020-03-29 14:20:36.000', 1, 'system.region.area.340822', '怀宁县', NULL, '340822', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1106, '2020-03-29 14:20:36.000', 1, 'system.region.area.340825', '太湖县', NULL, '340825', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1107, '2020-03-29 14:20:36.000', 1, 'system.region.area.340826', '宿松县', NULL, '340826', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1108, '2020-03-29 14:20:36.000', 1, 'system.region.area.340827', '望江县', NULL, '340827', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1109, '2020-03-29 14:20:36.000', 1, 'system.region.area.340828', '岳西县', NULL, '340828', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1110, '2020-03-29 14:20:36.000', 1, 'system.region.area.340881', '桐城市', NULL, '340881', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1111, '2020-03-29 14:20:36.000', 1, 'system.region.area.340882', '潜山市', NULL, '340882', 30, NULL, 1, 16, 1101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1112, '2020-03-29 14:20:36.000', 1, 'system.region.city.341000', '黄山市', 'area', '341000', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1113, '2020-03-29 14:20:36.000', 1, 'system.region.area.341002', '屯溪区', NULL, '341002', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1114, '2020-03-29 14:20:36.000', 1, 'system.region.area.341003', '黄山区', NULL, '341003', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1115, '2020-03-29 14:20:36.000', 1, 'system.region.area.341004', '徽州区', NULL, '341004', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1116, '2020-03-29 14:20:36.000', 1, 'system.region.area.341021', '歙县', NULL, '341021', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1117, '2020-03-29 14:20:36.000', 1, 'system.region.area.341022', '休宁县', NULL, '341022', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1118, '2020-03-29 14:20:36.000', 1, 'system.region.area.341023', '黟县', NULL, '341023', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1119, '2020-03-29 14:20:36.000', 1, 'system.region.area.341024', '祁门县', NULL, '341024', 30, NULL, 1, 16, 1112, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1120, '2020-03-29 14:20:36.000', 1, 'system.region.city.341100', '滁州市', 'area', '341100', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1121, '2020-03-29 14:20:36.000', 1, 'system.region.area.341102', '琅琊区', NULL, '341102', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1122, '2020-03-29 14:20:36.000', 1, 'system.region.area.341103', '南谯区', NULL, '341103', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1123, '2020-03-29 14:20:36.000', 1, 'system.region.area.341122', '来安县', NULL, '341122', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1124, '2020-03-29 14:20:36.000', 1, 'system.region.area.341124', '全椒县', NULL, '341124', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1125, '2020-03-29 14:20:36.000', 1, 'system.region.area.341125', '定远县', NULL, '341125', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1126, '2020-03-29 14:20:36.000', 1, 'system.region.area.341126', '凤阳县', NULL, '341126', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1127, '2020-03-29 14:20:36.000', 1, 'system.region.area.341181', '天长市', NULL, '341181', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1128, '2020-03-29 14:20:36.000', 1, 'system.region.area.341182', '明光市', NULL, '341182', 30, NULL, 1, 16, 1120, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1129, '2020-03-29 14:20:36.000', 1, 'system.region.city.341200', '阜阳市', 'area', '341200', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1130, '2020-03-29 14:20:36.000', 1, 'system.region.area.341202', '颍州区', NULL, '341202', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1131, '2020-03-29 14:20:36.000', 1, 'system.region.area.341203', '颍东区', NULL, '341203', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1132, '2020-03-29 14:20:36.000', 1, 'system.region.area.341204', '颍泉区', NULL, '341204', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1133, '2020-03-29 14:20:36.000', 1, 'system.region.area.341221', '临泉县', NULL, '341221', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1134, '2020-03-29 14:20:36.000', 1, 'system.region.area.341222', '太和县', NULL, '341222', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1135, '2020-03-29 14:20:36.000', 1, 'system.region.area.341225', '阜南县', NULL, '341225', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1136, '2020-03-29 14:20:36.000', 1, 'system.region.area.341226', '颍上县', NULL, '341226', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1137, '2020-03-29 14:20:36.000', 1, 'system.region.area.341282', '界首市', NULL, '341282', 30, NULL, 1, 16, 1129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1138, '2020-03-29 14:20:36.000', 1, 'system.region.city.341300', '宿州市', 'area', '341300', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1139, '2020-03-29 14:20:36.000', 1, 'system.region.area.341302', '埇桥区', NULL, '341302', 30, NULL, 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1140, '2020-03-29 14:20:36.000', 1, 'system.region.area.341321', '砀山县', NULL, '341321', 30, NULL, 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1141, '2020-03-29 14:20:36.000', 1, 'system.region.area.341322', '萧县', NULL, '341322', 30, NULL, 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1142, '2020-03-29 14:20:36.000', 1, 'system.region.area.341323', '灵璧县', NULL, '341323', 30, NULL, 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1143, '2020-03-29 14:20:36.000', 1, 'system.region.area.341324', '泗县', NULL, '341324', 30, NULL, 1, 16, 1138, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1144, '2020-03-29 14:20:36.000', 1, 'system.region.city.341500', '六安市', 'area', '341500', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1145, '2020-03-29 14:20:36.000', 1, 'system.region.area.341502', '金安区', NULL, '341502', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1146, '2020-03-29 14:20:36.000', 1, 'system.region.area.341503', '裕安区', NULL, '341503', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1147, '2020-03-29 14:20:36.000', 1, 'system.region.area.341504', '叶集区', NULL, '341504', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1148, '2020-03-29 14:20:36.000', 1, 'system.region.area.341522', '霍邱县', NULL, '341522', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1149, '2020-03-29 14:20:36.000', 1, 'system.region.area.341523', '舒城县', NULL, '341523', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1150, '2020-03-29 14:20:36.000', 1, 'system.region.area.341524', '金寨县', NULL, '341524', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1151, '2020-03-29 14:20:36.000', 1, 'system.region.area.341525', '霍山县', NULL, '341525', 30, NULL, 1, 16, 1144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1152, '2020-03-29 14:20:36.000', 1, 'system.region.city.341600', '亳州市', 'area', '341600', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1153, '2020-03-29 14:20:36.000', 1, 'system.region.area.341602', '谯城区', NULL, '341602', 30, NULL, 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1154, '2020-03-29 14:20:36.000', 1, 'system.region.area.341621', '涡阳县', NULL, '341621', 30, NULL, 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1155, '2020-03-29 14:20:36.000', 1, 'system.region.area.341622', '蒙城县', NULL, '341622', 30, NULL, 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1156, '2020-03-29 14:20:36.000', 1, 'system.region.area.341623', '利辛县', NULL, '341623', 30, NULL, 1, 16, 1152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1157, '2020-03-29 14:20:36.000', 1, 'system.region.city.341700', '池州市', 'area', '341700', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1158, '2020-03-29 14:20:36.000', 1, 'system.region.area.341702', '贵池区', NULL, '341702', 30, NULL, 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1159, '2020-03-29 14:20:36.000', 1, 'system.region.area.341721', '东至县', NULL, '341721', 30, NULL, 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1160, '2020-03-29 14:20:36.000', 1, 'system.region.area.341722', '石台县', NULL, '341722', 30, NULL, 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1161, '2020-03-29 14:20:36.000', 1, 'system.region.area.341723', '青阳县', NULL, '341723', 30, NULL, 1, 16, 1157, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1162, '2020-03-29 14:20:36.000', 1, 'system.region.city.341800', '宣城市', 'area', '341800', 30, NULL, 1, 15, 1048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1163, '2020-03-29 14:20:36.000', 1, 'system.region.area.341802', '宣州区', NULL, '341802', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1164, '2020-03-29 14:20:36.000', 1, 'system.region.area.341821', '郎溪县', NULL, '341821', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1165, '2020-03-29 14:20:36.000', 1, 'system.region.area.341823', '泾县', NULL, '341823', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1166, '2020-03-29 14:20:36.000', 1, 'system.region.area.341824', '绩溪县', NULL, '341824', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1167, '2020-03-29 14:20:36.000', 1, 'system.region.area.341825', '旌德县', NULL, '341825', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1168, '2020-03-29 14:20:36.000', 1, 'system.region.area.341881', '宁国市', NULL, '341881', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1169, '2020-03-29 14:20:36.000', 1, 'system.region.area.341882', '广德市', NULL, '341882', 30, NULL, 1, 16, 1162, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1170, '2020-03-29 14:20:36.000', 1, 'system.region.province.350000', '福建省', 'city', '350000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1171, '2020-03-29 14:20:36.000', 1, 'system.region.city.350100', '福州市', 'area', '350100', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1172, '2020-03-29 14:20:36.000', 1, 'system.region.area.350102', '鼓楼区', NULL, '350102', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1173, '2020-03-29 14:20:36.000', 1, 'system.region.area.350103', '台江区', NULL, '350103', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1174, '2020-03-29 14:20:36.000', 1, 'system.region.area.350104', '仓山区', NULL, '350104', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1175, '2020-03-29 14:20:36.000', 1, 'system.region.area.350105', '马尾区', NULL, '350105', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1176, '2020-03-29 14:20:36.000', 1, 'system.region.area.350111', '晋安区', NULL, '350111', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1177, '2020-03-29 14:20:36.000', 1, 'system.region.area.350112', '长乐区', NULL, '350112', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1178, '2020-03-29 14:20:36.000', 1, 'system.region.area.350121', '闽侯县', NULL, '350121', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1179, '2020-03-29 14:20:36.000', 1, 'system.region.area.350122', '连江县', NULL, '350122', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1180, '2020-03-29 14:20:36.000', 1, 'system.region.area.350123', '罗源县', NULL, '350123', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1181, '2020-03-29 14:20:36.000', 1, 'system.region.area.350124', '闽清县', NULL, '350124', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1182, '2020-03-29 14:20:36.000', 1, 'system.region.area.350125', '永泰县', NULL, '350125', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1183, '2020-03-29 14:20:36.000', 1, 'system.region.area.350128', '平潭县', NULL, '350128', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1184, '2020-03-29 14:20:36.000', 1, 'system.region.area.350181', '福清市', NULL, '350181', 30, NULL, 1, 16, 1171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1185, '2020-03-29 14:20:36.000', 1, 'system.region.city.350200', '厦门市', 'area', '350200', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1186, '2020-03-29 14:20:36.000', 1, 'system.region.area.350203', '思明区', NULL, '350203', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1187, '2020-03-29 14:20:36.000', 1, 'system.region.area.350205', '海沧区', NULL, '350205', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1188, '2020-03-29 14:20:36.000', 1, 'system.region.area.350206', '湖里区', NULL, '350206', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1189, '2020-03-29 14:20:36.000', 1, 'system.region.area.350211', '集美区', NULL, '350211', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1190, '2020-03-29 14:20:36.000', 1, 'system.region.area.350212', '同安区', NULL, '350212', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1191, '2020-03-29 14:20:36.000', 1, 'system.region.area.350213', '翔安区', NULL, '350213', 30, NULL, 1, 16, 1185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1192, '2020-03-29 14:20:36.000', 1, 'system.region.city.350300', '莆田市', 'area', '350300', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1193, '2020-03-29 14:20:36.000', 1, 'system.region.area.350302', '城厢区', NULL, '350302', 30, NULL, 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1194, '2020-03-29 14:20:36.000', 1, 'system.region.area.350303', '涵江区', NULL, '350303', 30, NULL, 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1195, '2020-03-29 14:20:36.000', 1, 'system.region.area.350304', '荔城区', NULL, '350304', 30, NULL, 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1196, '2020-03-29 14:20:36.000', 1, 'system.region.area.350305', '秀屿区', NULL, '350305', 30, NULL, 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1197, '2020-03-29 14:20:36.000', 1, 'system.region.area.350322', '仙游县', NULL, '350322', 30, NULL, 1, 16, 1192, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1198, '2020-03-29 14:20:36.000', 1, 'system.region.city.350400', '三明市', 'area', '350400', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1199, '2020-03-29 14:20:36.000', 1, 'system.region.area.350402', '梅列区', NULL, '350402', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1200, '2020-03-29 14:20:36.000', 1, 'system.region.area.350403', '三元区', NULL, '350403', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1201, '2020-03-29 14:20:36.000', 1, 'system.region.area.350421', '明溪县', NULL, '350421', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1202, '2020-03-29 14:20:36.000', 1, 'system.region.area.350423', '清流县', NULL, '350423', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1203, '2020-03-29 14:20:36.000', 1, 'system.region.area.350424', '宁化县', NULL, '350424', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1204, '2020-03-29 14:20:36.000', 1, 'system.region.area.350425', '大田县', NULL, '350425', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1205, '2020-03-29 14:20:36.000', 1, 'system.region.area.350426', '尤溪县', NULL, '350426', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1206, '2020-03-29 14:20:36.000', 1, 'system.region.area.350427', '沙县', NULL, '350427', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1207, '2020-03-29 14:20:36.000', 1, 'system.region.area.350428', '将乐县', NULL, '350428', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1208, '2020-03-29 14:20:36.000', 1, 'system.region.area.350429', '泰宁县', NULL, '350429', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1209, '2020-03-29 14:20:36.000', 1, 'system.region.area.350430', '建宁县', NULL, '350430', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1210, '2020-03-29 14:20:36.000', 1, 'system.region.area.350481', '永安市', NULL, '350481', 30, NULL, 1, 16, 1198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1211, '2020-03-29 14:20:36.000', 1, 'system.region.city.350500', '泉州市', 'area', '350500', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1212, '2020-03-29 14:20:36.000', 1, 'system.region.area.350502', '鲤城区', NULL, '350502', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1213, '2020-03-29 14:20:36.000', 1, 'system.region.area.350503', '丰泽区', NULL, '350503', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1214, '2020-03-29 14:20:36.000', 1, 'system.region.area.350504', '洛江区', NULL, '350504', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1215, '2020-03-29 14:20:36.000', 1, 'system.region.area.350505', '泉港区', NULL, '350505', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1216, '2020-03-29 14:20:36.000', 1, 'system.region.area.350521', '惠安县', NULL, '350521', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1217, '2020-03-29 14:20:36.000', 1, 'system.region.area.350524', '安溪县', NULL, '350524', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1218, '2020-03-29 14:20:36.000', 1, 'system.region.area.350525', '永春县', NULL, '350525', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1219, '2020-03-29 14:20:36.000', 1, 'system.region.area.350526', '德化县', NULL, '350526', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1220, '2020-03-29 14:20:36.000', 1, 'system.region.area.350527', '金门县', NULL, '350527', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1221, '2020-03-29 14:20:36.000', 1, 'system.region.area.350581', '石狮市', NULL, '350581', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1222, '2020-03-29 14:20:36.000', 1, 'system.region.area.350582', '晋江市', NULL, '350582', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1223, '2020-03-29 14:20:36.000', 1, 'system.region.area.350583', '南安市', NULL, '350583', 30, NULL, 1, 16, 1211, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1224, '2020-03-29 14:20:36.000', 1, 'system.region.city.350600', '漳州市', 'area', '350600', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1225, '2020-03-29 14:20:36.000', 1, 'system.region.area.350602', '芗城区', NULL, '350602', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1226, '2020-03-29 14:20:36.000', 1, 'system.region.area.350603', '龙文区', NULL, '350603', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1227, '2020-03-29 14:20:36.000', 1, 'system.region.area.350622', '云霄县', NULL, '350622', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1228, '2020-03-29 14:20:36.000', 1, 'system.region.area.350623', '漳浦县', NULL, '350623', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1229, '2020-03-29 14:20:36.000', 1, 'system.region.area.350624', '诏安县', NULL, '350624', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1230, '2020-03-29 14:20:36.000', 1, 'system.region.area.350625', '长泰县', NULL, '350625', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1231, '2020-03-29 14:20:36.000', 1, 'system.region.area.350626', '东山县', NULL, '350626', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1232, '2020-03-29 14:20:36.000', 1, 'system.region.area.350627', '南靖县', NULL, '350627', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1233, '2020-03-29 14:20:36.000', 1, 'system.region.area.350628', '平和县', NULL, '350628', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1234, '2020-03-29 14:20:36.000', 1, 'system.region.area.350629', '华安县', NULL, '350629', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1235, '2020-03-29 14:20:36.000', 1, 'system.region.area.350681', '龙海市', NULL, '350681', 30, NULL, 1, 16, 1224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1236, '2020-03-29 14:20:36.000', 1, 'system.region.city.350700', '南平市', 'area', '350700', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1237, '2020-03-29 14:20:36.000', 1, 'system.region.area.350702', '延平区', NULL, '350702', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1238, '2020-03-29 14:20:36.000', 1, 'system.region.area.350703', '建阳区', NULL, '350703', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1239, '2020-03-29 14:20:36.000', 1, 'system.region.area.350721', '顺昌县', NULL, '350721', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1240, '2020-03-29 14:20:36.000', 1, 'system.region.area.350722', '浦城县', NULL, '350722', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1241, '2020-03-29 14:20:36.000', 1, 'system.region.area.350723', '光泽县', NULL, '350723', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1242, '2020-03-29 14:20:36.000', 1, 'system.region.area.350724', '松溪县', NULL, '350724', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1243, '2020-03-29 14:20:36.000', 1, 'system.region.area.350725', '政和县', NULL, '350725', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1244, '2020-03-29 14:20:36.000', 1, 'system.region.area.350781', '邵武市', NULL, '350781', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1245, '2020-03-29 14:20:36.000', 1, 'system.region.area.350782', '武夷山市', NULL, '350782', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1246, '2020-03-29 14:20:36.000', 1, 'system.region.area.350783', '建瓯市', NULL, '350783', 30, NULL, 1, 16, 1236, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1247, '2020-03-29 14:20:36.000', 1, 'system.region.city.350800', '龙岩市', 'area', '350800', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1248, '2020-03-29 14:20:36.000', 1, 'system.region.area.350802', '新罗区', NULL, '350802', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1249, '2020-03-29 14:20:36.000', 1, 'system.region.area.350803', '永定区', NULL, '350803', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1250, '2020-03-29 14:20:36.000', 1, 'system.region.area.350821', '长汀县', NULL, '350821', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1251, '2020-03-29 14:20:36.000', 1, 'system.region.area.350823', '上杭县', NULL, '350823', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1252, '2020-03-29 14:20:36.000', 1, 'system.region.area.350824', '武平县', NULL, '350824', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1253, '2020-03-29 14:20:36.000', 1, 'system.region.area.350825', '连城县', NULL, '350825', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1254, '2020-03-29 14:20:36.000', 1, 'system.region.area.350881', '漳平市', NULL, '350881', 30, NULL, 1, 16, 1247, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1255, '2020-03-29 14:20:36.000', 1, 'system.region.city.350900', '宁德市', 'area', '350900', 30, NULL, 1, 15, 1170, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1256, '2020-03-29 14:20:36.000', 1, 'system.region.area.350902', '蕉城区', NULL, '350902', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1257, '2020-03-29 14:20:36.000', 1, 'system.region.area.350921', '霞浦县', NULL, '350921', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1258, '2020-03-29 14:20:36.000', 1, 'system.region.area.350922', '古田县', NULL, '350922', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1259, '2020-03-29 14:20:36.000', 1, 'system.region.area.350923', '屏南县', NULL, '350923', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1260, '2020-03-29 14:20:36.000', 1, 'system.region.area.350924', '寿宁县', NULL, '350924', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1261, '2020-03-29 14:20:36.000', 1, 'system.region.area.350925', '周宁县', NULL, '350925', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1262, '2020-03-29 14:20:36.000', 1, 'system.region.area.350926', '柘荣县', NULL, '350926', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1263, '2020-03-29 14:20:36.000', 1, 'system.region.area.350981', '福安市', NULL, '350981', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1264, '2020-03-29 14:20:36.000', 1, 'system.region.area.350982', '福鼎市', NULL, '350982', 30, NULL, 1, 16, 1255, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1265, '2020-03-29 14:20:36.000', 1, 'system.region.province.360000', '江西省', 'city', '360000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1266, '2020-03-29 14:20:36.000', 1, 'system.region.city.360100', '南昌市', 'area', '360100', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1267, '2020-03-29 14:20:36.000', 1, 'system.region.area.360102', '东湖区', NULL, '360102', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1268, '2020-03-29 14:20:36.000', 1, 'system.region.area.360103', '西湖区', NULL, '360103', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1269, '2020-03-29 14:20:36.000', 1, 'system.region.area.360104', '青云谱区', NULL, '360104', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1270, '2020-03-29 14:20:36.000', 1, 'system.region.area.360111', '青山湖区', NULL, '360111', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1271, '2020-03-29 14:20:36.000', 1, 'system.region.area.360112', '新建区', NULL, '360112', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1272, '2020-03-29 14:20:36.000', 1, 'system.region.area.360113', '红谷滩区', NULL, '360113', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1273, '2020-03-29 14:20:36.000', 1, 'system.region.area.360121', '南昌县', NULL, '360121', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1274, '2020-03-29 14:20:36.000', 1, 'system.region.area.360123', '安义县', NULL, '360123', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1275, '2020-03-29 14:20:36.000', 1, 'system.region.area.360124', '进贤县', NULL, '360124', 30, NULL, 1, 16, 1266, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1276, '2020-03-29 14:20:36.000', 1, 'system.region.city.360200', '景德镇市', 'area', '360200', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1277, '2020-03-29 14:20:36.000', 1, 'system.region.area.360202', '昌江区', NULL, '360202', 30, NULL, 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1278, '2020-03-29 14:20:36.000', 1, 'system.region.area.360203', '珠山区', NULL, '360203', 30, NULL, 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1279, '2020-03-29 14:20:36.000', 1, 'system.region.area.360222', '浮梁县', NULL, '360222', 30, NULL, 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1280, '2020-03-29 14:20:36.000', 1, 'system.region.area.360281', '乐平市', NULL, '360281', 30, NULL, 1, 16, 1276, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1281, '2020-03-29 14:20:36.000', 1, 'system.region.city.360300', '萍乡市', 'area', '360300', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1282, '2020-03-29 14:20:36.000', 1, 'system.region.area.360302', '安源区', NULL, '360302', 30, NULL, 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1283, '2020-03-29 14:20:36.000', 1, 'system.region.area.360313', '湘东区', NULL, '360313', 30, NULL, 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1284, '2020-03-29 14:20:36.000', 1, 'system.region.area.360321', '莲花县', NULL, '360321', 30, NULL, 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1285, '2020-03-29 14:20:36.000', 1, 'system.region.area.360322', '上栗县', NULL, '360322', 30, NULL, 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1286, '2020-03-29 14:20:36.000', 1, 'system.region.area.360323', '芦溪县', NULL, '360323', 30, NULL, 1, 16, 1281, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1287, '2020-03-29 14:20:36.000', 1, 'system.region.city.360400', '九江市', 'area', '360400', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1288, '2020-03-29 14:20:36.000', 1, 'system.region.area.360402', '濂溪区', NULL, '360402', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1289, '2020-03-29 14:20:36.000', 1, 'system.region.area.360403', '浔阳区', NULL, '360403', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1290, '2020-03-29 14:20:36.000', 1, 'system.region.area.360404', '柴桑区', NULL, '360404', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1291, '2020-03-29 14:20:36.000', 1, 'system.region.area.360423', '武宁县', NULL, '360423', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1292, '2020-03-29 14:20:36.000', 1, 'system.region.area.360424', '修水县', NULL, '360424', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1293, '2020-03-29 14:20:36.000', 1, 'system.region.area.360425', '永修县', NULL, '360425', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1294, '2020-03-29 14:20:36.000', 1, 'system.region.area.360426', '德安县', NULL, '360426', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1295, '2020-03-29 14:20:36.000', 1, 'system.region.area.360428', '都昌县', NULL, '360428', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1296, '2020-03-29 14:20:36.000', 1, 'system.region.area.360429', '湖口县', NULL, '360429', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1297, '2020-03-29 14:20:36.000', 1, 'system.region.area.360430', '彭泽县', NULL, '360430', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1298, '2020-03-29 14:20:36.000', 1, 'system.region.area.360481', '瑞昌市', NULL, '360481', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1299, '2020-03-29 14:20:36.000', 1, 'system.region.area.360482', '共青城市', NULL, '360482', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1300, '2020-03-29 14:20:36.000', 1, 'system.region.area.360483', '庐山市', NULL, '360483', 30, NULL, 1, 16, 1287, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1301, '2020-03-29 14:20:36.000', 1, 'system.region.city.360500', '新余市', 'area', '360500', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1302, '2020-03-29 14:20:36.000', 1, 'system.region.area.360502', '渝水区', NULL, '360502', 30, NULL, 1, 16, 1301, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1303, '2020-03-29 14:20:36.000', 1, 'system.region.area.360521', '分宜县', NULL, '360521', 30, NULL, 1, 16, 1301, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1304, '2020-03-29 14:20:36.000', 1, 'system.region.city.360600', '鹰潭市', 'area', '360600', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1305, '2020-03-29 14:20:36.000', 1, 'system.region.area.360602', '月湖区', NULL, '360602', 30, NULL, 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1306, '2020-03-29 14:20:36.000', 1, 'system.region.area.360603', '余江区', NULL, '360603', 30, NULL, 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1307, '2020-03-29 14:20:36.000', 1, 'system.region.area.360681', '贵溪市', NULL, '360681', 30, NULL, 1, 16, 1304, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1308, '2020-03-29 14:20:36.000', 1, 'system.region.city.360700', '赣州市', 'area', '360700', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1309, '2020-03-29 14:20:36.000', 1, 'system.region.area.360702', '章贡区', NULL, '360702', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1310, '2020-03-29 14:20:36.000', 1, 'system.region.area.360703', '南康区', NULL, '360703', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1311, '2020-03-29 14:20:36.000', 1, 'system.region.area.360704', '赣县区', NULL, '360704', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1312, '2020-03-29 14:20:36.000', 1, 'system.region.area.360722', '信丰县', NULL, '360722', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1313, '2020-03-29 14:20:36.000', 1, 'system.region.area.360723', '大余县', NULL, '360723', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1314, '2020-03-29 14:20:36.000', 1, 'system.region.area.360724', '上犹县', NULL, '360724', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1315, '2020-03-29 14:20:36.000', 1, 'system.region.area.360725', '崇义县', NULL, '360725', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1316, '2020-03-29 14:20:36.000', 1, 'system.region.area.360726', '安远县', NULL, '360726', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1317, '2020-03-29 14:20:36.000', 1, 'system.region.area.360727', '龙南县', NULL, '360727', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1318, '2020-03-29 14:20:36.000', 1, 'system.region.area.360728', '定南县', NULL, '360728', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1319, '2020-03-29 14:20:36.000', 1, 'system.region.area.360729', '全南县', NULL, '360729', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1320, '2020-03-29 14:20:36.000', 1, 'system.region.area.360730', '宁都县', NULL, '360730', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1321, '2020-03-29 14:20:36.000', 1, 'system.region.area.360731', '于都县', NULL, '360731', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1322, '2020-03-29 14:20:36.000', 1, 'system.region.area.360732', '兴国县', NULL, '360732', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1323, '2020-03-29 14:20:36.000', 1, 'system.region.area.360733', '会昌县', NULL, '360733', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1324, '2020-03-29 14:20:36.000', 1, 'system.region.area.360734', '寻乌县', NULL, '360734', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1325, '2020-03-29 14:20:36.000', 1, 'system.region.area.360735', '石城县', NULL, '360735', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1326, '2020-03-29 14:20:36.000', 1, 'system.region.area.360781', '瑞金市', NULL, '360781', 30, NULL, 1, 16, 1308, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1327, '2020-03-29 14:20:36.000', 1, 'system.region.city.360800', '吉安市', 'area', '360800', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1328, '2020-03-29 14:20:36.000', 1, 'system.region.area.360802', '吉州区', NULL, '360802', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1329, '2020-03-29 14:20:36.000', 1, 'system.region.area.360803', '青原区', NULL, '360803', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1330, '2020-03-29 14:20:36.000', 1, 'system.region.area.360821', '吉安县', NULL, '360821', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1331, '2020-03-29 14:20:36.000', 1, 'system.region.area.360822', '吉水县', NULL, '360822', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1332, '2020-03-29 14:20:36.000', 1, 'system.region.area.360823', '峡江县', NULL, '360823', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1333, '2020-03-29 14:20:36.000', 1, 'system.region.area.360824', '新干县', NULL, '360824', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1334, '2020-03-29 14:20:36.000', 1, 'system.region.area.360825', '永丰县', NULL, '360825', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1335, '2020-03-29 14:20:36.000', 1, 'system.region.area.360826', '泰和县', NULL, '360826', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1336, '2020-03-29 14:20:36.000', 1, 'system.region.area.360827', '遂川县', NULL, '360827', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1337, '2020-03-29 14:20:36.000', 1, 'system.region.area.360828', '万安县', NULL, '360828', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1338, '2020-03-29 14:20:36.000', 1, 'system.region.area.360829', '安福县', NULL, '360829', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1339, '2020-03-29 14:20:36.000', 1, 'system.region.area.360830', '永新县', NULL, '360830', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1340, '2020-03-29 14:20:36.000', 1, 'system.region.area.360881', '井冈山市', NULL, '360881', 30, NULL, 1, 16, 1327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1341, '2020-03-29 14:20:36.000', 1, 'system.region.city.360900', '宜春市', 'area', '360900', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1342, '2020-03-29 14:20:36.000', 1, 'system.region.area.360902', '袁州区', NULL, '360902', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1343, '2020-03-29 14:20:36.000', 1, 'system.region.area.360921', '奉新县', NULL, '360921', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1344, '2020-03-29 14:20:36.000', 1, 'system.region.area.360922', '万载县', NULL, '360922', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1345, '2020-03-29 14:20:36.000', 1, 'system.region.area.360923', '上高县', NULL, '360923', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1346, '2020-03-29 14:20:36.000', 1, 'system.region.area.360924', '宜丰县', NULL, '360924', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1347, '2020-03-29 14:20:36.000', 1, 'system.region.area.360925', '靖安县', NULL, '360925', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1348, '2020-03-29 14:20:36.000', 1, 'system.region.area.360926', '铜鼓县', NULL, '360926', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1349, '2020-03-29 14:20:36.000', 1, 'system.region.area.360981', '丰城市', NULL, '360981', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1350, '2020-03-29 14:20:36.000', 1, 'system.region.area.360982', '樟树市', NULL, '360982', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1351, '2020-03-29 14:20:36.000', 1, 'system.region.area.360983', '高安市', NULL, '360983', 30, NULL, 1, 16, 1341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1352, '2020-03-29 14:20:36.000', 1, 'system.region.city.361000', '抚州市', 'area', '361000', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1353, '2020-03-29 14:20:36.000', 1, 'system.region.area.361002', '临川区', NULL, '361002', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1354, '2020-03-29 14:20:36.000', 1, 'system.region.area.361003', '东乡区', NULL, '361003', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1355, '2020-03-29 14:20:36.000', 1, 'system.region.area.361021', '南城县', NULL, '361021', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1356, '2020-03-29 14:20:36.000', 1, 'system.region.area.361022', '黎川县', NULL, '361022', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1357, '2020-03-29 14:20:36.000', 1, 'system.region.area.361023', '南丰县', NULL, '361023', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1358, '2020-03-29 14:20:36.000', 1, 'system.region.area.361024', '崇仁县', NULL, '361024', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1359, '2020-03-29 14:20:36.000', 1, 'system.region.area.361025', '乐安县', NULL, '361025', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1360, '2020-03-29 14:20:36.000', 1, 'system.region.area.361026', '宜黄县', NULL, '361026', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1361, '2020-03-29 14:20:36.000', 1, 'system.region.area.361027', '金溪县', NULL, '361027', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1362, '2020-03-29 14:20:36.000', 1, 'system.region.area.361028', '资溪县', NULL, '361028', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1363, '2020-03-29 14:20:36.000', 1, 'system.region.area.361030', '广昌县', NULL, '361030', 30, NULL, 1, 16, 1352, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1364, '2020-03-29 14:20:36.000', 1, 'system.region.city.361100', '上饶市', 'area', '361100', 30, NULL, 1, 15, 1265, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1365, '2020-03-29 14:20:36.000', 1, 'system.region.area.361102', '信州区', NULL, '361102', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1366, '2020-03-29 14:20:36.000', 1, 'system.region.area.361103', '广丰区', NULL, '361103', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1367, '2020-03-29 14:20:36.000', 1, 'system.region.area.361104', '广信区', NULL, '361104', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1368, '2020-03-29 14:20:36.000', 1, 'system.region.area.361123', '玉山县', NULL, '361123', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1369, '2020-03-29 14:20:36.000', 1, 'system.region.area.361124', '铅山县', NULL, '361124', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1370, '2020-03-29 14:20:36.000', 1, 'system.region.area.361125', '横峰县', NULL, '361125', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1371, '2020-03-29 14:20:36.000', 1, 'system.region.area.361126', '弋阳县', NULL, '361126', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1372, '2020-03-29 14:20:36.000', 1, 'system.region.area.361127', '余干县', NULL, '361127', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1373, '2020-03-29 14:20:36.000', 1, 'system.region.area.361128', '鄱阳县', NULL, '361128', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1374, '2020-03-29 14:20:36.000', 1, 'system.region.area.361129', '万年县', NULL, '361129', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1375, '2020-03-29 14:20:36.000', 1, 'system.region.area.361130', '婺源县', NULL, '361130', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1376, '2020-03-29 14:20:36.000', 1, 'system.region.area.361181', '德兴市', NULL, '361181', 30, NULL, 1, 16, 1364, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1377, '2020-03-29 14:20:36.000', 1, 'system.region.province.370000', '山东省', 'city', '370000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1378, '2020-03-29 14:20:36.000', 1, 'system.region.city.370100', '济南市', 'area', '370100', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1379, '2020-03-29 14:20:36.000', 1, 'system.region.area.370102', '历下区', NULL, '370102', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1380, '2020-03-29 14:20:36.000', 1, 'system.region.area.370103', '市中区', NULL, '370103', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1381, '2020-03-29 14:20:36.000', 1, 'system.region.area.370104', '槐荫区', NULL, '370104', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1382, '2020-03-29 14:20:36.000', 1, 'system.region.area.370105', '天桥区', NULL, '370105', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1383, '2020-03-29 14:20:36.000', 1, 'system.region.area.370112', '历城区', NULL, '370112', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1384, '2020-03-29 14:20:36.000', 1, 'system.region.area.370113', '长清区', NULL, '370113', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1385, '2020-03-29 14:20:36.000', 1, 'system.region.area.370114', '章丘区', NULL, '370114', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1386, '2020-03-29 14:20:36.000', 1, 'system.region.area.370115', '济阳区', NULL, '370115', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1387, '2020-03-29 14:20:36.000', 1, 'system.region.area.370116', '莱芜区', NULL, '370116', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1388, '2020-03-29 14:20:36.000', 1, 'system.region.area.370117', '钢城区', NULL, '370117', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1389, '2020-03-29 14:20:36.000', 1, 'system.region.area.370124', '平阴县', NULL, '370124', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1390, '2020-03-29 14:20:36.000', 1, 'system.region.area.370126', '商河县', NULL, '370126', 30, NULL, 1, 16, 1378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1391, '2020-03-29 14:20:36.000', 1, 'system.region.city.370200', '青岛市', 'area', '370200', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1392, '2020-03-29 14:20:36.000', 1, 'system.region.area.370202', '市南区', NULL, '370202', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1393, '2020-03-29 14:20:36.000', 1, 'system.region.area.370203', '市北区', NULL, '370203', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1394, '2020-03-29 14:20:36.000', 1, 'system.region.area.370211', '黄岛区', NULL, '370211', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1395, '2020-03-29 14:20:36.000', 1, 'system.region.area.370212', '崂山区', NULL, '370212', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1396, '2020-03-29 14:20:36.000', 1, 'system.region.area.370213', '李沧区', NULL, '370213', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1397, '2020-03-29 14:20:36.000', 1, 'system.region.area.370214', '城阳区', NULL, '370214', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1398, '2020-03-29 14:20:36.000', 1, 'system.region.area.370215', '即墨区', NULL, '370215', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1399, '2020-03-29 14:20:36.000', 1, 'system.region.area.370281', '胶州市', NULL, '370281', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1400, '2020-03-29 14:20:36.000', 1, 'system.region.area.370283', '平度市', NULL, '370283', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1401, '2020-03-29 14:20:36.000', 1, 'system.region.area.370285', '莱西市', NULL, '370285', 30, NULL, 1, 16, 1391, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1402, '2020-03-29 14:20:36.000', 1, 'system.region.city.370300', '淄博市', 'area', '370300', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1403, '2020-03-29 14:20:36.000', 1, 'system.region.area.370302', '淄川区', NULL, '370302', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1404, '2020-03-29 14:20:36.000', 1, 'system.region.area.370303', '张店区', NULL, '370303', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1405, '2020-03-29 14:20:36.000', 1, 'system.region.area.370304', '博山区', NULL, '370304', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1406, '2020-03-29 14:20:36.000', 1, 'system.region.area.370305', '临淄区', NULL, '370305', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1407, '2020-03-29 14:20:36.000', 1, 'system.region.area.370306', '周村区', NULL, '370306', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1408, '2020-03-29 14:20:36.000', 1, 'system.region.area.370321', '桓台县', NULL, '370321', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1409, '2020-03-29 14:20:36.000', 1, 'system.region.area.370322', '高青县', NULL, '370322', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1410, '2020-03-29 14:20:36.000', 1, 'system.region.area.370323', '沂源县', NULL, '370323', 30, NULL, 1, 16, 1402, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1411, '2020-03-29 14:20:36.000', 1, 'system.region.city.370400', '枣庄市', 'area', '370400', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1412, '2020-03-29 14:20:36.000', 1, 'system.region.area.370402', '市中区', NULL, '370402', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1413, '2020-03-29 14:20:36.000', 1, 'system.region.area.370403', '薛城区', NULL, '370403', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1414, '2020-03-29 14:20:36.000', 1, 'system.region.area.370404', '峄城区', NULL, '370404', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1415, '2020-03-29 14:20:36.000', 1, 'system.region.area.370405', '台儿庄区', NULL, '370405', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1416, '2020-03-29 14:20:36.000', 1, 'system.region.area.370406', '山亭区', NULL, '370406', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1417, '2020-03-29 14:20:36.000', 1, 'system.region.area.370481', '滕州市', NULL, '370481', 30, NULL, 1, 16, 1411, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1418, '2020-03-29 14:20:36.000', 1, 'system.region.city.370500', '东营市', 'area', '370500', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1419, '2020-03-29 14:20:36.000', 1, 'system.region.area.370502', '东营区', NULL, '370502', 30, NULL, 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1420, '2020-03-29 14:20:36.000', 1, 'system.region.area.370503', '河口区', NULL, '370503', 30, NULL, 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1421, '2020-03-29 14:20:36.000', 1, 'system.region.area.370505', '垦利区', NULL, '370505', 30, NULL, 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1422, '2020-03-29 14:20:36.000', 1, 'system.region.area.370522', '利津县', NULL, '370522', 30, NULL, 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1423, '2020-03-29 14:20:36.000', 1, 'system.region.area.370523', '广饶县', NULL, '370523', 30, NULL, 1, 16, 1418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1424, '2020-03-29 14:20:36.000', 1, 'system.region.city.370600', '烟台市', 'area', '370600', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1425, '2020-03-29 14:20:36.000', 1, 'system.region.area.370602', '芝罘区', NULL, '370602', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1426, '2020-03-29 14:20:36.000', 1, 'system.region.area.370611', '福山区', NULL, '370611', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1427, '2020-03-29 14:20:36.000', 1, 'system.region.area.370612', '牟平区', NULL, '370612', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1428, '2020-03-29 14:20:36.000', 1, 'system.region.area.370613', '莱山区', NULL, '370613', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1429, '2020-03-29 14:20:36.000', 1, 'system.region.area.370634', '长岛县', NULL, '370634', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1430, '2020-03-29 14:20:36.000', 1, 'system.region.area.370681', '龙口市', NULL, '370681', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1431, '2020-03-29 14:20:36.000', 1, 'system.region.area.370682', '莱阳市', NULL, '370682', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1432, '2020-03-29 14:20:36.000', 1, 'system.region.area.370683', '莱州市', NULL, '370683', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1433, '2020-03-29 14:20:36.000', 1, 'system.region.area.370684', '蓬莱市', NULL, '370684', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1434, '2020-03-29 14:20:36.000', 1, 'system.region.area.370685', '招远市', NULL, '370685', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1435, '2020-03-29 14:20:36.000', 1, 'system.region.area.370686', '栖霞市', NULL, '370686', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1436, '2020-03-29 14:20:36.000', 1, 'system.region.area.370687', '海阳市', NULL, '370687', 30, NULL, 1, 16, 1424, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1437, '2020-03-29 14:20:36.000', 1, 'system.region.city.370700', '潍坊市', 'area', '370700', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1438, '2020-03-29 14:20:36.000', 1, 'system.region.area.370702', '潍城区', NULL, '370702', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1439, '2020-03-29 14:20:36.000', 1, 'system.region.area.370703', '寒亭区', NULL, '370703', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1440, '2020-03-29 14:20:36.000', 1, 'system.region.area.370704', '坊子区', NULL, '370704', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1441, '2020-03-29 14:20:36.000', 1, 'system.region.area.370705', '奎文区', NULL, '370705', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1442, '2020-03-29 14:20:36.000', 1, 'system.region.area.370724', '临朐县', NULL, '370724', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1443, '2020-03-29 14:20:36.000', 1, 'system.region.area.370725', '昌乐县', NULL, '370725', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1444, '2020-03-29 14:20:36.000', 1, 'system.region.area.370781', '青州市', NULL, '370781', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1445, '2020-03-29 14:20:36.000', 1, 'system.region.area.370782', '诸城市', NULL, '370782', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1446, '2020-03-29 14:20:36.000', 1, 'system.region.area.370783', '寿光市', NULL, '370783', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1447, '2020-03-29 14:20:36.000', 1, 'system.region.area.370784', '安丘市', NULL, '370784', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1448, '2020-03-29 14:20:36.000', 1, 'system.region.area.370785', '高密市', NULL, '370785', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1449, '2020-03-29 14:20:36.000', 1, 'system.region.area.370786', '昌邑市', NULL, '370786', 30, NULL, 1, 16, 1437, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1450, '2020-03-29 14:20:36.000', 1, 'system.region.city.370800', '济宁市', 'area', '370800', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1451, '2020-03-29 14:20:36.000', 1, 'system.region.area.370811', '任城区', NULL, '370811', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1452, '2020-03-29 14:20:36.000', 1, 'system.region.area.370812', '兖州区', NULL, '370812', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1453, '2020-03-29 14:20:36.000', 1, 'system.region.area.370826', '微山县', NULL, '370826', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1454, '2020-03-29 14:20:36.000', 1, 'system.region.area.370827', '鱼台县', NULL, '370827', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1455, '2020-03-29 14:20:36.000', 1, 'system.region.area.370828', '金乡县', NULL, '370828', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1456, '2020-03-29 14:20:36.000', 1, 'system.region.area.370829', '嘉祥县', NULL, '370829', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1457, '2020-03-29 14:20:36.000', 1, 'system.region.area.370830', '汶上县', NULL, '370830', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1458, '2020-03-29 14:20:36.000', 1, 'system.region.area.370831', '泗水县', NULL, '370831', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1459, '2020-03-29 14:20:36.000', 1, 'system.region.area.370832', '梁山县', NULL, '370832', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1460, '2020-03-29 14:20:36.000', 1, 'system.region.area.370881', '曲阜市', NULL, '370881', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1461, '2020-03-29 14:20:36.000', 1, 'system.region.area.370883', '邹城市', NULL, '370883', 30, NULL, 1, 16, 1450, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1462, '2020-03-29 14:20:36.000', 1, 'system.region.city.370900', '泰安市', 'area', '370900', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1463, '2020-03-29 14:20:36.000', 1, 'system.region.area.370902', '泰山区', NULL, '370902', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1464, '2020-03-29 14:20:36.000', 1, 'system.region.area.370911', '岱岳区', NULL, '370911', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1465, '2020-03-29 14:20:36.000', 1, 'system.region.area.370921', '宁阳县', NULL, '370921', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1466, '2020-03-29 14:20:36.000', 1, 'system.region.area.370923', '东平县', NULL, '370923', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1467, '2020-03-29 14:20:36.000', 1, 'system.region.area.370982', '新泰市', NULL, '370982', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1468, '2020-03-29 14:20:36.000', 1, 'system.region.area.370983', '肥城市', NULL, '370983', 30, NULL, 1, 16, 1462, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1469, '2020-03-29 14:20:36.000', 1, 'system.region.city.371000', '威海市', 'area', '371000', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1470, '2020-03-29 14:20:36.000', 1, 'system.region.area.371002', '环翠区', NULL, '371002', 30, NULL, 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1471, '2020-03-29 14:20:36.000', 1, 'system.region.area.371003', '文登区', NULL, '371003', 30, NULL, 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1472, '2020-03-29 14:20:36.000', 1, 'system.region.area.371082', '荣成市', NULL, '371082', 30, NULL, 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1473, '2020-03-29 14:20:36.000', 1, 'system.region.area.371083', '乳山市', NULL, '371083', 30, NULL, 1, 16, 1469, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1474, '2020-03-29 14:20:36.000', 1, 'system.region.city.371100', '日照市', 'area', '371100', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1475, '2020-03-29 14:20:36.000', 1, 'system.region.area.371102', '东港区', NULL, '371102', 30, NULL, 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1476, '2020-03-29 14:20:36.000', 1, 'system.region.area.371103', '岚山区', NULL, '371103', 30, NULL, 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1477, '2020-03-29 14:20:36.000', 1, 'system.region.area.371121', '五莲县', NULL, '371121', 30, NULL, 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1478, '2020-03-29 14:20:36.000', 1, 'system.region.area.371122', '莒县', NULL, '371122', 30, NULL, 1, 16, 1474, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1479, '2020-03-29 14:20:36.000', 1, 'system.region.city.371300', '临沂市', 'area', '371300', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1480, '2020-03-29 14:20:36.000', 1, 'system.region.area.371302', '兰山区', NULL, '371302', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1481, '2020-03-29 14:20:36.000', 1, 'system.region.area.371311', '罗庄区', NULL, '371311', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1482, '2020-03-29 14:20:36.000', 1, 'system.region.area.371312', '河东区', NULL, '371312', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1483, '2020-03-29 14:20:36.000', 1, 'system.region.area.371321', '沂南县', NULL, '371321', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1484, '2020-03-29 14:20:36.000', 1, 'system.region.area.371322', '郯城县', NULL, '371322', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1485, '2020-03-29 14:20:36.000', 1, 'system.region.area.371323', '沂水县', NULL, '371323', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1486, '2020-03-29 14:20:36.000', 1, 'system.region.area.371324', '兰陵县', NULL, '371324', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1487, '2020-03-29 14:20:36.000', 1, 'system.region.area.371325', '费县', NULL, '371325', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1488, '2020-03-29 14:20:36.000', 1, 'system.region.area.371326', '平邑县', NULL, '371326', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1489, '2020-03-29 14:20:36.000', 1, 'system.region.area.371327', '莒南县', NULL, '371327', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1490, '2020-03-29 14:20:36.000', 1, 'system.region.area.371328', '蒙阴县', NULL, '371328', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1491, '2020-03-29 14:20:36.000', 1, 'system.region.area.371329', '临沭县', NULL, '371329', 30, NULL, 1, 16, 1479, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1492, '2020-03-29 14:20:36.000', 1, 'system.region.city.371400', '德州市', 'area', '371400', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1493, '2020-03-29 14:20:36.000', 1, 'system.region.area.371402', '德城区', NULL, '371402', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1494, '2020-03-29 14:20:36.000', 1, 'system.region.area.371403', '陵城区', NULL, '371403', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1495, '2020-03-29 14:20:36.000', 1, 'system.region.area.371422', '宁津县', NULL, '371422', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1496, '2020-03-29 14:20:36.000', 1, 'system.region.area.371423', '庆云县', NULL, '371423', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1497, '2020-03-29 14:20:36.000', 1, 'system.region.area.371424', '临邑县', NULL, '371424', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1498, '2020-03-29 14:20:36.000', 1, 'system.region.area.371425', '齐河县', NULL, '371425', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1499, '2020-03-29 14:20:36.000', 1, 'system.region.area.371426', '平原县', NULL, '371426', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1500, '2020-03-29 14:20:36.000', 1, 'system.region.area.371427', '夏津县', NULL, '371427', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1501, '2020-03-29 14:20:36.000', 1, 'system.region.area.371428', '武城县', NULL, '371428', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1502, '2020-03-29 14:20:36.000', 1, 'system.region.area.371481', '乐陵市', NULL, '371481', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1503, '2020-03-29 14:20:36.000', 1, 'system.region.area.371482', '禹城市', NULL, '371482', 30, NULL, 1, 16, 1492, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1504, '2020-03-29 14:20:36.000', 1, 'system.region.city.371500', '聊城市', 'area', '371500', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1505, '2020-03-29 14:20:36.000', 1, 'system.region.area.371502', '东昌府区', NULL, '371502', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1506, '2020-03-29 14:20:36.000', 1, 'system.region.area.371503', '茌平区', NULL, '371503', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1507, '2020-03-29 14:20:36.000', 1, 'system.region.area.371521', '阳谷县', NULL, '371521', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1508, '2020-03-29 14:20:36.000', 1, 'system.region.area.371522', '莘县', NULL, '371522', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1509, '2020-03-29 14:20:36.000', 1, 'system.region.area.371524', '东阿县', NULL, '371524', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1510, '2020-03-29 14:20:36.000', 1, 'system.region.area.371525', '冠县', NULL, '371525', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1511, '2020-03-29 14:20:36.000', 1, 'system.region.area.371526', '高唐县', NULL, '371526', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1512, '2020-03-29 14:20:36.000', 1, 'system.region.area.371581', '临清市', NULL, '371581', 30, NULL, 1, 16, 1504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1513, '2020-03-29 14:20:36.000', 1, 'system.region.city.371600', '滨州市', 'area', '371600', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1514, '2020-03-29 14:20:36.000', 1, 'system.region.area.371602', '滨城区', NULL, '371602', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1515, '2020-03-29 14:20:36.000', 1, 'system.region.area.371603', '沾化区', NULL, '371603', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1516, '2020-03-29 14:20:36.000', 1, 'system.region.area.371621', '惠民县', NULL, '371621', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1517, '2020-03-29 14:20:36.000', 1, 'system.region.area.371622', '阳信县', NULL, '371622', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1518, '2020-03-29 14:20:36.000', 1, 'system.region.area.371623', '无棣县', NULL, '371623', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1519, '2020-03-29 14:20:36.000', 1, 'system.region.area.371625', '博兴县', NULL, '371625', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1520, '2020-03-29 14:20:36.000', 1, 'system.region.area.371681', '邹平市', NULL, '371681', 30, NULL, 1, 16, 1513, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1521, '2020-03-29 14:20:36.000', 1, 'system.region.city.371700', '菏泽市', 'area', '371700', 30, NULL, 1, 15, 1377, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1522, '2020-03-29 14:20:36.000', 1, 'system.region.area.371702', '牡丹区', NULL, '371702', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1523, '2020-03-29 14:20:36.000', 1, 'system.region.area.371703', '定陶区', NULL, '371703', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1524, '2020-03-29 14:20:36.000', 1, 'system.region.area.371721', '曹县', NULL, '371721', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1525, '2020-03-29 14:20:36.000', 1, 'system.region.area.371722', '单县', NULL, '371722', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1526, '2020-03-29 14:20:36.000', 1, 'system.region.area.371723', '成武县', NULL, '371723', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1527, '2020-03-29 14:20:36.000', 1, 'system.region.area.371724', '巨野县', NULL, '371724', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1528, '2020-03-29 14:20:36.000', 1, 'system.region.area.371725', '郓城县', NULL, '371725', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1529, '2020-03-29 14:20:36.000', 1, 'system.region.area.371726', '鄄城县', NULL, '371726', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1530, '2020-03-29 14:20:36.000', 1, 'system.region.area.371728', '东明县', NULL, '371728', 30, NULL, 1, 16, 1521, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1531, '2020-03-29 14:20:36.000', 1, 'system.region.province.410000', '河南省', 'city', '410000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1532, '2020-03-29 14:20:36.000', 1, 'system.region.city.410100', '郑州市', 'area', '410100', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1533, '2020-03-29 14:20:36.000', 1, 'system.region.area.410102', '中原区', NULL, '410102', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1534, '2020-03-29 14:20:36.000', 1, 'system.region.area.410103', '二七区', NULL, '410103', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1535, '2020-03-29 14:20:36.000', 1, 'system.region.area.410104', '管城回族区', NULL, '410104', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1536, '2020-03-29 14:20:36.000', 1, 'system.region.area.410105', '金水区', NULL, '410105', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1537, '2020-03-29 14:20:36.000', 1, 'system.region.area.410106', '上街区', NULL, '410106', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1538, '2020-03-29 14:20:36.000', 1, 'system.region.area.410108', '惠济区', NULL, '410108', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1539, '2020-03-29 14:20:36.000', 1, 'system.region.area.410122', '中牟县', NULL, '410122', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1540, '2020-03-29 14:20:36.000', 1, 'system.region.area.410181', '巩义市', NULL, '410181', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1541, '2020-03-29 14:20:36.000', 1, 'system.region.area.410182', '荥阳市', NULL, '410182', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1542, '2020-03-29 14:20:36.000', 1, 'system.region.area.410183', '新密市', NULL, '410183', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1543, '2020-03-29 14:20:36.000', 1, 'system.region.area.410184', '新郑市', NULL, '410184', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1544, '2020-03-29 14:20:36.000', 1, 'system.region.area.410185', '登封市', NULL, '410185', 30, NULL, 1, 16, 1532, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1545, '2020-03-29 14:20:36.000', 1, 'system.region.city.410200', '开封市', 'area', '410200', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1546, '2020-03-29 14:20:36.000', 1, 'system.region.area.410202', '龙亭区', NULL, '410202', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1547, '2020-03-29 14:20:36.000', 1, 'system.region.area.410203', '顺河回族区', NULL, '410203', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1548, '2020-03-29 14:20:36.000', 1, 'system.region.area.410204', '鼓楼区', NULL, '410204', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1549, '2020-03-29 14:20:36.000', 1, 'system.region.area.410205', '禹王台区', NULL, '410205', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1550, '2020-03-29 14:20:36.000', 1, 'system.region.area.410212', '祥符区', NULL, '410212', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1551, '2020-03-29 14:20:36.000', 1, 'system.region.area.410221', '杞县', NULL, '410221', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1552, '2020-03-29 14:20:36.000', 1, 'system.region.area.410222', '通许县', NULL, '410222', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1553, '2020-03-29 14:20:36.000', 1, 'system.region.area.410223', '尉氏县', NULL, '410223', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1554, '2020-03-29 14:20:36.000', 1, 'system.region.area.410225', '兰考县', NULL, '410225', 30, NULL, 1, 16, 1545, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1555, '2020-03-29 14:20:36.000', 1, 'system.region.city.410300', '洛阳市', 'area', '410300', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1556, '2020-03-29 14:20:36.000', 1, 'system.region.area.410302', '老城区', NULL, '410302', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1557, '2020-03-29 14:20:36.000', 1, 'system.region.area.410303', '西工区', NULL, '410303', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1558, '2020-03-29 14:20:36.000', 1, 'system.region.area.410304', '瀍河回族区', NULL, '410304', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1559, '2020-03-29 14:20:36.000', 1, 'system.region.area.410305', '涧西区', NULL, '410305', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1560, '2020-03-29 14:20:36.000', 1, 'system.region.area.410306', '吉利区', NULL, '410306', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1561, '2020-03-29 14:20:36.000', 1, 'system.region.area.410311', '洛龙区', NULL, '410311', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1562, '2020-03-29 14:20:36.000', 1, 'system.region.area.410322', '孟津县', NULL, '410322', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1563, '2020-03-29 14:20:36.000', 1, 'system.region.area.410323', '新安县', NULL, '410323', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1564, '2020-03-29 14:20:36.000', 1, 'system.region.area.410324', '栾川县', NULL, '410324', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1565, '2020-03-29 14:20:36.000', 1, 'system.region.area.410325', '嵩县', NULL, '410325', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1566, '2020-03-29 14:20:36.000', 1, 'system.region.area.410326', '汝阳县', NULL, '410326', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1567, '2020-03-29 14:20:36.000', 1, 'system.region.area.410327', '宜阳县', NULL, '410327', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1568, '2020-03-29 14:20:36.000', 1, 'system.region.area.410328', '洛宁县', NULL, '410328', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1569, '2020-03-29 14:20:36.000', 1, 'system.region.area.410329', '伊川县', NULL, '410329', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1570, '2020-03-29 14:20:36.000', 1, 'system.region.area.410381', '偃师市', NULL, '410381', 30, NULL, 1, 16, 1555, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1571, '2020-03-29 14:20:36.000', 1, 'system.region.city.410400', '平顶山市', 'area', '410400', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1572, '2020-03-29 14:20:36.000', 1, 'system.region.area.410402', '新华区', NULL, '410402', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1573, '2020-03-29 14:20:36.000', 1, 'system.region.area.410403', '卫东区', NULL, '410403', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1574, '2020-03-29 14:20:36.000', 1, 'system.region.area.410404', '石龙区', NULL, '410404', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1575, '2020-03-29 14:20:36.000', 1, 'system.region.area.410411', '湛河区', NULL, '410411', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1576, '2020-03-29 14:20:36.000', 1, 'system.region.area.410421', '宝丰县', NULL, '410421', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1577, '2020-03-29 14:20:36.000', 1, 'system.region.area.410422', '叶县', NULL, '410422', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1578, '2020-03-29 14:20:36.000', 1, 'system.region.area.410423', '鲁山县', NULL, '410423', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1579, '2020-03-29 14:20:36.000', 1, 'system.region.area.410425', '郏县', NULL, '410425', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1580, '2020-03-29 14:20:36.000', 1, 'system.region.area.410481', '舞钢市', NULL, '410481', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1581, '2020-03-29 14:20:36.000', 1, 'system.region.area.410482', '汝州市', NULL, '410482', 30, NULL, 1, 16, 1571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1582, '2020-03-29 14:20:36.000', 1, 'system.region.city.410500', '安阳市', 'area', '410500', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1583, '2020-03-29 14:20:36.000', 1, 'system.region.area.410502', '文峰区', NULL, '410502', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1584, '2020-03-29 14:20:36.000', 1, 'system.region.area.410503', '北关区', NULL, '410503', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1585, '2020-03-29 14:20:36.000', 1, 'system.region.area.410505', '殷都区', NULL, '410505', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1586, '2020-03-29 14:20:36.000', 1, 'system.region.area.410506', '龙安区', NULL, '410506', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1587, '2020-03-29 14:20:36.000', 1, 'system.region.area.410522', '安阳县', NULL, '410522', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1588, '2020-03-29 14:20:36.000', 1, 'system.region.area.410523', '汤阴县', NULL, '410523', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1589, '2020-03-29 14:20:36.000', 1, 'system.region.area.410526', '滑县', NULL, '410526', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1590, '2020-03-29 14:20:36.000', 1, 'system.region.area.410527', '内黄县', NULL, '410527', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1591, '2020-03-29 14:20:36.000', 1, 'system.region.area.410581', '林州市', NULL, '410581', 30, NULL, 1, 16, 1582, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1592, '2020-03-29 14:20:36.000', 1, 'system.region.city.410600', '鹤壁市', 'area', '410600', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1593, '2020-03-29 14:20:36.000', 1, 'system.region.area.410602', '鹤山区', NULL, '410602', 30, NULL, 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1594, '2020-03-29 14:20:36.000', 1, 'system.region.area.410603', '山城区', NULL, '410603', 30, NULL, 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1595, '2020-03-29 14:20:36.000', 1, 'system.region.area.410611', '淇滨区', NULL, '410611', 30, NULL, 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1596, '2020-03-29 14:20:36.000', 1, 'system.region.area.410621', '浚县', NULL, '410621', 30, NULL, 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1597, '2020-03-29 14:20:36.000', 1, 'system.region.area.410622', '淇县', NULL, '410622', 30, NULL, 1, 16, 1592, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1598, '2020-03-29 14:20:36.000', 1, 'system.region.city.410700', '新乡市', 'area', '410700', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1599, '2020-03-29 14:20:36.000', 1, 'system.region.area.410702', '红旗区', NULL, '410702', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1600, '2020-03-29 14:20:36.000', 1, 'system.region.area.410703', '卫滨区', NULL, '410703', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1601, '2020-03-29 14:20:36.000', 1, 'system.region.area.410704', '凤泉区', NULL, '410704', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1602, '2020-03-29 14:20:36.000', 1, 'system.region.area.410711', '牧野区', NULL, '410711', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1603, '2020-03-29 14:20:36.000', 1, 'system.region.area.410721', '新乡县', NULL, '410721', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1604, '2020-03-29 14:20:36.000', 1, 'system.region.area.410724', '获嘉县', NULL, '410724', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1605, '2020-03-29 14:20:36.000', 1, 'system.region.area.410725', '原阳县', NULL, '410725', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1606, '2020-03-29 14:20:36.000', 1, 'system.region.area.410726', '延津县', NULL, '410726', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1607, '2020-03-29 14:20:36.000', 1, 'system.region.area.410727', '封丘县', NULL, '410727', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1608, '2020-03-29 14:20:36.000', 1, 'system.region.area.410781', '卫辉市', NULL, '410781', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1609, '2020-03-29 14:20:36.000', 1, 'system.region.area.410782', '辉县市', NULL, '410782', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1610, '2020-03-29 14:20:36.000', 1, 'system.region.area.410783', '长垣市', NULL, '410783', 30, NULL, 1, 16, 1598, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1611, '2020-03-29 14:20:36.000', 1, 'system.region.city.410800', '焦作市', 'area', '410800', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1612, '2020-03-29 14:20:36.000', 1, 'system.region.area.410802', '解放区', NULL, '410802', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1613, '2020-03-29 14:20:36.000', 1, 'system.region.area.410803', '中站区', NULL, '410803', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1614, '2020-03-29 14:20:36.000', 1, 'system.region.area.410804', '马村区', NULL, '410804', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1615, '2020-03-29 14:20:36.000', 1, 'system.region.area.410811', '山阳区', NULL, '410811', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1616, '2020-03-29 14:20:36.000', 1, 'system.region.area.410821', '修武县', NULL, '410821', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1617, '2020-03-29 14:20:36.000', 1, 'system.region.area.410822', '博爱县', NULL, '410822', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1618, '2020-03-29 14:20:36.000', 1, 'system.region.area.410823', '武陟县', NULL, '410823', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1619, '2020-03-29 14:20:36.000', 1, 'system.region.area.410825', '温县', NULL, '410825', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1620, '2020-03-29 14:20:36.000', 1, 'system.region.area.410882', '沁阳市', NULL, '410882', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1621, '2020-03-29 14:20:36.000', 1, 'system.region.area.410883', '孟州市', NULL, '410883', 30, NULL, 1, 16, 1611, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1622, '2020-03-29 14:20:36.000', 1, 'system.region.city.410900', '濮阳市', 'area', '410900', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1623, '2020-03-29 14:20:36.000', 1, 'system.region.area.410902', '华龙区', NULL, '410902', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1624, '2020-03-29 14:20:36.000', 1, 'system.region.area.410922', '清丰县', NULL, '410922', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1625, '2020-03-29 14:20:36.000', 1, 'system.region.area.410923', '南乐县', NULL, '410923', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1626, '2020-03-29 14:20:36.000', 1, 'system.region.area.410926', '范县', NULL, '410926', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1627, '2020-03-29 14:20:36.000', 1, 'system.region.area.410927', '台前县', NULL, '410927', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1628, '2020-03-29 14:20:36.000', 1, 'system.region.area.410928', '濮阳县', NULL, '410928', 30, NULL, 1, 16, 1622, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1629, '2020-03-29 14:20:36.000', 1, 'system.region.city.411000', '许昌市', 'area', '411000', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1630, '2020-03-29 14:20:36.000', 1, 'system.region.area.411002', '魏都区', NULL, '411002', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1631, '2020-03-29 14:20:36.000', 1, 'system.region.area.411003', '建安区', NULL, '411003', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1632, '2020-03-29 14:20:36.000', 1, 'system.region.area.411024', '鄢陵县', NULL, '411024', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1633, '2020-03-29 14:20:36.000', 1, 'system.region.area.411025', '襄城县', NULL, '411025', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1634, '2020-03-29 14:20:36.000', 1, 'system.region.area.411081', '禹州市', NULL, '411081', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1635, '2020-03-29 14:20:36.000', 1, 'system.region.area.411082', '长葛市', NULL, '411082', 30, NULL, 1, 16, 1629, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1636, '2020-03-29 14:20:36.000', 1, 'system.region.city.411100', '漯河市', 'area', '411100', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1637, '2020-03-29 14:20:36.000', 1, 'system.region.area.411102', '源汇区', NULL, '411102', 30, NULL, 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1638, '2020-03-29 14:20:36.000', 1, 'system.region.area.411103', '郾城区', NULL, '411103', 30, NULL, 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1639, '2020-03-29 14:20:36.000', 1, 'system.region.area.411104', '召陵区', NULL, '411104', 30, NULL, 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1640, '2020-03-29 14:20:36.000', 1, 'system.region.area.411121', '舞阳县', NULL, '411121', 30, NULL, 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1641, '2020-03-29 14:20:36.000', 1, 'system.region.area.411122', '临颍县', NULL, '411122', 30, NULL, 1, 16, 1636, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1642, '2020-03-29 14:20:36.000', 1, 'system.region.city.411200', '三门峡市', 'area', '411200', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1643, '2020-03-29 14:20:36.000', 1, 'system.region.area.411202', '湖滨区', NULL, '411202', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1644, '2020-03-29 14:20:36.000', 1, 'system.region.area.411203', '陕州区', NULL, '411203', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1645, '2020-03-29 14:20:36.000', 1, 'system.region.area.411221', '渑池县', NULL, '411221', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1646, '2020-03-29 14:20:36.000', 1, 'system.region.area.411224', '卢氏县', NULL, '411224', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1647, '2020-03-29 14:20:36.000', 1, 'system.region.area.411281', '义马市', NULL, '411281', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1648, '2020-03-29 14:20:36.000', 1, 'system.region.area.411282', '灵宝市', NULL, '411282', 30, NULL, 1, 16, 1642, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1649, '2020-03-29 14:20:36.000', 1, 'system.region.city.411300', '南阳市', 'area', '411300', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1650, '2020-03-29 14:20:36.000', 1, 'system.region.area.411302', '宛城区', NULL, '411302', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1651, '2020-03-29 14:20:36.000', 1, 'system.region.area.411303', '卧龙区', NULL, '411303', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1652, '2020-03-29 14:20:36.000', 1, 'system.region.area.411321', '南召县', NULL, '411321', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1653, '2020-03-29 14:20:36.000', 1, 'system.region.area.411322', '方城县', NULL, '411322', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1654, '2020-03-29 14:20:36.000', 1, 'system.region.area.411323', '西峡县', NULL, '411323', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1655, '2020-03-29 14:20:36.000', 1, 'system.region.area.411324', '镇平县', NULL, '411324', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1656, '2020-03-29 14:20:36.000', 1, 'system.region.area.411325', '内乡县', NULL, '411325', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1657, '2020-03-29 14:20:36.000', 1, 'system.region.area.411326', '淅川县', NULL, '411326', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1658, '2020-03-29 14:20:36.000', 1, 'system.region.area.411327', '社旗县', NULL, '411327', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1659, '2020-03-29 14:20:36.000', 1, 'system.region.area.411328', '唐河县', NULL, '411328', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1660, '2020-03-29 14:20:36.000', 1, 'system.region.area.411329', '新野县', NULL, '411329', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1661, '2020-03-29 14:20:36.000', 1, 'system.region.area.411330', '桐柏县', NULL, '411330', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1662, '2020-03-29 14:20:36.000', 1, 'system.region.area.411381', '邓州市', NULL, '411381', 30, NULL, 1, 16, 1649, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1663, '2020-03-29 14:20:36.000', 1, 'system.region.city.411400', '商丘市', 'area', '411400', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1664, '2020-03-29 14:20:36.000', 1, 'system.region.area.411402', '梁园区', NULL, '411402', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1665, '2020-03-29 14:20:36.000', 1, 'system.region.area.411403', '睢阳区', NULL, '411403', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1666, '2020-03-29 14:20:36.000', 1, 'system.region.area.411421', '民权县', NULL, '411421', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1667, '2020-03-29 14:20:36.000', 1, 'system.region.area.411422', '睢县', NULL, '411422', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1668, '2020-03-29 14:20:36.000', 1, 'system.region.area.411423', '宁陵县', NULL, '411423', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1669, '2020-03-29 14:20:36.000', 1, 'system.region.area.411424', '柘城县', NULL, '411424', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1670, '2020-03-29 14:20:36.000', 1, 'system.region.area.411425', '虞城县', NULL, '411425', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1671, '2020-03-29 14:20:36.000', 1, 'system.region.area.411426', '夏邑县', NULL, '411426', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1672, '2020-03-29 14:20:36.000', 1, 'system.region.area.411481', '永城市', NULL, '411481', 30, NULL, 1, 16, 1663, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1673, '2020-03-29 14:20:36.000', 1, 'system.region.city.411500', '信阳市', 'area', '411500', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1674, '2020-03-29 14:20:36.000', 1, 'system.region.area.411502', '浉河区', NULL, '411502', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1675, '2020-03-29 14:20:36.000', 1, 'system.region.area.411503', '平桥区', NULL, '411503', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1676, '2020-03-29 14:20:36.000', 1, 'system.region.area.411521', '罗山县', NULL, '411521', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1677, '2020-03-29 14:20:36.000', 1, 'system.region.area.411522', '光山县', NULL, '411522', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1678, '2020-03-29 14:20:36.000', 1, 'system.region.area.411523', '新县', NULL, '411523', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1679, '2020-03-29 14:20:36.000', 1, 'system.region.area.411524', '商城县', NULL, '411524', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1680, '2020-03-29 14:20:36.000', 1, 'system.region.area.411525', '固始县', NULL, '411525', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1681, '2020-03-29 14:20:36.000', 1, 'system.region.area.411526', '潢川县', NULL, '411526', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1682, '2020-03-29 14:20:36.000', 1, 'system.region.area.411527', '淮滨县', NULL, '411527', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1683, '2020-03-29 14:20:36.000', 1, 'system.region.area.411528', '息县', NULL, '411528', 30, NULL, 1, 16, 1673, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1684, '2020-03-29 14:20:36.000', 1, 'system.region.city.411600', '周口市', 'area', '411600', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1685, '2020-03-29 14:20:36.000', 1, 'system.region.area.411602', '川汇区', NULL, '411602', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1686, '2020-03-29 14:20:36.000', 1, 'system.region.area.411603', '淮阳区', NULL, '411603', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1687, '2020-03-29 14:20:36.000', 1, 'system.region.area.411621', '扶沟县', NULL, '411621', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1688, '2020-03-29 14:20:36.000', 1, 'system.region.area.411622', '西华县', NULL, '411622', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1689, '2020-03-29 14:20:36.000', 1, 'system.region.area.411623', '商水县', NULL, '411623', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1690, '2020-03-29 14:20:36.000', 1, 'system.region.area.411624', '沈丘县', NULL, '411624', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1691, '2020-03-29 14:20:36.000', 1, 'system.region.area.411625', '郸城县', NULL, '411625', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1692, '2020-03-29 14:20:36.000', 1, 'system.region.area.411627', '太康县', NULL, '411627', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1693, '2020-03-29 14:20:36.000', 1, 'system.region.area.411628', '鹿邑县', NULL, '411628', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1694, '2020-03-29 14:20:36.000', 1, 'system.region.area.411681', '项城市', NULL, '411681', 30, NULL, 1, 16, 1684, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1695, '2020-03-29 14:20:36.000', 1, 'system.region.city.411700', '驻马店市', 'area', '411700', 30, NULL, 1, 15, 1531, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1696, '2020-03-29 14:20:36.000', 1, 'system.region.area.411702', '驿城区', NULL, '411702', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1697, '2020-03-29 14:20:36.000', 1, 'system.region.area.411721', '西平县', NULL, '411721', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1698, '2020-03-29 14:20:36.000', 1, 'system.region.area.411722', '上蔡县', NULL, '411722', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1699, '2020-03-29 14:20:36.000', 1, 'system.region.area.411723', '平舆县', NULL, '411723', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1700, '2020-03-29 14:20:36.000', 1, 'system.region.area.411724', '正阳县', NULL, '411724', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1701, '2020-03-29 14:20:36.000', 1, 'system.region.area.411725', '确山县', NULL, '411725', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1702, '2020-03-29 14:20:36.000', 1, 'system.region.area.411726', '泌阳县', NULL, '411726', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1703, '2020-03-29 14:20:36.000', 1, 'system.region.area.411727', '汝南县', NULL, '411727', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1704, '2020-03-29 14:20:36.000', 1, 'system.region.area.411728', '遂平县', NULL, '411728', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1705, '2020-03-29 14:20:36.000', 1, 'system.region.area.411729', '新蔡县', NULL, '411729', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1706, '2020-03-29 14:20:36.000', 1, 'system.region.area.419001', '济源市', NULL, '419001', 30, NULL, 1, 16, 1695, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1707, '2020-03-29 14:20:36.000', 1, 'system.region.province.420000', '湖北省', 'city', '420000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1708, '2020-03-29 14:20:36.000', 1, 'system.region.city.420100', '武汉市', 'area', '420100', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1709, '2020-03-29 14:20:36.000', 1, 'system.region.area.420102', '江岸区', NULL, '420102', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1710, '2020-03-29 14:20:36.000', 1, 'system.region.area.420103', '江汉区', NULL, '420103', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1711, '2020-03-29 14:20:36.000', 1, 'system.region.area.420104', '硚口区', NULL, '420104', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1712, '2020-03-29 14:20:36.000', 1, 'system.region.area.420105', '汉阳区', NULL, '420105', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1713, '2020-03-29 14:20:36.000', 1, 'system.region.area.420106', '武昌区', NULL, '420106', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1714, '2020-03-29 14:20:36.000', 1, 'system.region.area.420107', '青山区', NULL, '420107', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1715, '2020-03-29 14:20:36.000', 1, 'system.region.area.420111', '洪山区', NULL, '420111', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1716, '2020-03-29 14:20:36.000', 1, 'system.region.area.420112', '东西湖区', NULL, '420112', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1717, '2020-03-29 14:20:36.000', 1, 'system.region.area.420113', '汉南区', NULL, '420113', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1718, '2020-03-29 14:20:36.000', 1, 'system.region.area.420114', '蔡甸区', NULL, '420114', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1719, '2020-03-29 14:20:36.000', 1, 'system.region.area.420115', '江夏区', NULL, '420115', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1720, '2020-03-29 14:20:36.000', 1, 'system.region.area.420116', '黄陂区', NULL, '420116', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1721, '2020-03-29 14:20:36.000', 1, 'system.region.area.420117', '新洲区', NULL, '420117', 30, NULL, 1, 16, 1708, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1722, '2020-03-29 14:20:36.000', 1, 'system.region.city.420200', '黄石市', 'area', '420200', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1723, '2020-03-29 14:20:36.000', 1, 'system.region.area.420202', '黄石港区', NULL, '420202', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1724, '2020-03-29 14:20:36.000', 1, 'system.region.area.420203', '西塞山区', NULL, '420203', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1725, '2020-03-29 14:20:36.000', 1, 'system.region.area.420204', '下陆区', NULL, '420204', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1726, '2020-03-29 14:20:36.000', 1, 'system.region.area.420205', '铁山区', NULL, '420205', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1727, '2020-03-29 14:20:36.000', 1, 'system.region.area.420222', '阳新县', NULL, '420222', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1728, '2020-03-29 14:20:36.000', 1, 'system.region.area.420281', '大冶市', NULL, '420281', 30, NULL, 1, 16, 1722, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1729, '2020-03-29 14:20:36.000', 1, 'system.region.city.420300', '十堰市', 'area', '420300', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1730, '2020-03-29 14:20:36.000', 1, 'system.region.area.420302', '茅箭区', NULL, '420302', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1731, '2020-03-29 14:20:36.000', 1, 'system.region.area.420303', '张湾区', NULL, '420303', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1732, '2020-03-29 14:20:36.000', 1, 'system.region.area.420304', '郧阳区', NULL, '420304', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1733, '2020-03-29 14:20:36.000', 1, 'system.region.area.420322', '郧西县', NULL, '420322', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1734, '2020-03-29 14:20:36.000', 1, 'system.region.area.420323', '竹山县', NULL, '420323', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1735, '2020-03-29 14:20:36.000', 1, 'system.region.area.420324', '竹溪县', NULL, '420324', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1736, '2020-03-29 14:20:36.000', 1, 'system.region.area.420325', '房县', NULL, '420325', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1737, '2020-03-29 14:20:36.000', 1, 'system.region.area.420381', '丹江口市', NULL, '420381', 30, NULL, 1, 16, 1729, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1738, '2020-03-29 14:20:36.000', 1, 'system.region.city.420500', '宜昌市', 'area', '420500', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1739, '2020-03-29 14:20:36.000', 1, 'system.region.area.420502', '西陵区', NULL, '420502', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1740, '2020-03-29 14:20:36.000', 1, 'system.region.area.420503', '伍家岗区', NULL, '420503', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1741, '2020-03-29 14:20:36.000', 1, 'system.region.area.420504', '点军区', NULL, '420504', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1742, '2020-03-29 14:20:36.000', 1, 'system.region.area.420505', '猇亭区', NULL, '420505', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1743, '2020-03-29 14:20:36.000', 1, 'system.region.area.420506', '夷陵区', NULL, '420506', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1744, '2020-03-29 14:20:36.000', 1, 'system.region.area.420525', '远安县', NULL, '420525', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1745, '2020-03-29 14:20:36.000', 1, 'system.region.area.420526', '兴山县', NULL, '420526', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1746, '2020-03-29 14:20:36.000', 1, 'system.region.area.420527', '秭归县', NULL, '420527', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1747, '2020-03-29 14:20:36.000', 1, 'system.region.area.420528', '长阳土家族自治县', NULL, '420528', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1748, '2020-03-29 14:20:36.000', 1, 'system.region.area.420529', '五峰土家族自治县', NULL, '420529', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1749, '2020-03-29 14:20:36.000', 1, 'system.region.area.420581', '宜都市', NULL, '420581', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1750, '2020-03-29 14:20:36.000', 1, 'system.region.area.420582', '当阳市', NULL, '420582', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1751, '2020-03-29 14:20:36.000', 1, 'system.region.area.420583', '枝江市', NULL, '420583', 30, NULL, 1, 16, 1738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1752, '2020-03-29 14:20:36.000', 1, 'system.region.city.420600', '襄阳市', 'area', '420600', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1753, '2020-03-29 14:20:36.000', 1, 'system.region.area.420602', '襄城区', NULL, '420602', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1754, '2020-03-29 14:20:36.000', 1, 'system.region.area.420606', '樊城区', NULL, '420606', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1755, '2020-03-29 14:20:36.000', 1, 'system.region.area.420607', '襄州区', NULL, '420607', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1756, '2020-03-29 14:20:36.000', 1, 'system.region.area.420624', '南漳县', NULL, '420624', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1757, '2020-03-29 14:20:36.000', 1, 'system.region.area.420625', '谷城县', NULL, '420625', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1758, '2020-03-29 14:20:36.000', 1, 'system.region.area.420626', '保康县', NULL, '420626', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1759, '2020-03-29 14:20:36.000', 1, 'system.region.area.420682', '老河口市', NULL, '420682', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1760, '2020-03-29 14:20:36.000', 1, 'system.region.area.420683', '枣阳市', NULL, '420683', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1761, '2020-03-29 14:20:36.000', 1, 'system.region.area.420684', '宜城市', NULL, '420684', 30, NULL, 1, 16, 1752, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1762, '2020-03-29 14:20:36.000', 1, 'system.region.city.420700', '鄂州市', 'area', '420700', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1763, '2020-03-29 14:20:36.000', 1, 'system.region.area.420702', '梁子湖区', NULL, '420702', 30, NULL, 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1764, '2020-03-29 14:20:36.000', 1, 'system.region.area.420703', '华容区', NULL, '420703', 30, NULL, 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1765, '2020-03-29 14:20:36.000', 1, 'system.region.area.420704', '鄂城区', NULL, '420704', 30, NULL, 1, 16, 1762, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1766, '2020-03-29 14:20:36.000', 1, 'system.region.city.420800', '荆门市', 'area', '420800', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1767, '2020-03-29 14:20:36.000', 1, 'system.region.area.420802', '东宝区', NULL, '420802', 30, NULL, 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1768, '2020-03-29 14:20:36.000', 1, 'system.region.area.420804', '掇刀区', NULL, '420804', 30, NULL, 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1769, '2020-03-29 14:20:36.000', 1, 'system.region.area.420822', '沙洋县', NULL, '420822', 30, NULL, 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1770, '2020-03-29 14:20:36.000', 1, 'system.region.area.420881', '钟祥市', NULL, '420881', 30, NULL, 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1771, '2020-03-29 14:20:36.000', 1, 'system.region.area.420882', '京山市', NULL, '420882', 30, NULL, 1, 16, 1766, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1772, '2020-03-29 14:20:36.000', 1, 'system.region.city.420900', '孝感市', 'area', '420900', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1773, '2020-03-29 14:20:36.000', 1, 'system.region.area.420902', '孝南区', NULL, '420902', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1774, '2020-03-29 14:20:36.000', 1, 'system.region.area.420921', '孝昌县', NULL, '420921', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1775, '2020-03-29 14:20:36.000', 1, 'system.region.area.420922', '大悟县', NULL, '420922', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1776, '2020-03-29 14:20:36.000', 1, 'system.region.area.420923', '云梦县', NULL, '420923', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1777, '2020-03-29 14:20:36.000', 1, 'system.region.area.420981', '应城市', NULL, '420981', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1778, '2020-03-29 14:20:36.000', 1, 'system.region.area.420982', '安陆市', NULL, '420982', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1779, '2020-03-29 14:20:36.000', 1, 'system.region.area.420984', '汉川市', NULL, '420984', 30, NULL, 1, 16, 1772, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1780, '2020-03-29 14:20:36.000', 1, 'system.region.city.421000', '荆州市', 'area', '421000', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1781, '2020-03-29 14:20:36.000', 1, 'system.region.area.421002', '沙市区', NULL, '421002', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1782, '2020-03-29 14:20:36.000', 1, 'system.region.area.421003', '荆州区', NULL, '421003', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1783, '2020-03-29 14:20:36.000', 1, 'system.region.area.421022', '公安县', NULL, '421022', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1784, '2020-03-29 14:20:36.000', 1, 'system.region.area.421023', '监利县', NULL, '421023', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1785, '2020-03-29 14:20:36.000', 1, 'system.region.area.421024', '江陵县', NULL, '421024', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1786, '2020-03-29 14:20:36.000', 1, 'system.region.area.421081', '石首市', NULL, '421081', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1787, '2020-03-29 14:20:36.000', 1, 'system.region.area.421083', '洪湖市', NULL, '421083', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1788, '2020-03-29 14:20:36.000', 1, 'system.region.area.421087', '松滋市', NULL, '421087', 30, NULL, 1, 16, 1780, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1789, '2020-03-29 14:20:36.000', 1, 'system.region.city.421100', '黄冈市', 'area', '421100', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1790, '2020-03-29 14:20:36.000', 1, 'system.region.area.421102', '黄州区', NULL, '421102', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1791, '2020-03-29 14:20:36.000', 1, 'system.region.area.421121', '团风县', NULL, '421121', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1792, '2020-03-29 14:20:36.000', 1, 'system.region.area.421122', '红安县', NULL, '421122', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1793, '2020-03-29 14:20:36.000', 1, 'system.region.area.421123', '罗田县', NULL, '421123', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1794, '2020-03-29 14:20:36.000', 1, 'system.region.area.421124', '英山县', NULL, '421124', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1795, '2020-03-29 14:20:36.000', 1, 'system.region.area.421125', '浠水县', NULL, '421125', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1796, '2020-03-29 14:20:36.000', 1, 'system.region.area.421126', '蕲春县', NULL, '421126', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1797, '2020-03-29 14:20:36.000', 1, 'system.region.area.421127', '黄梅县', NULL, '421127', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1798, '2020-03-29 14:20:36.000', 1, 'system.region.area.421181', '麻城市', NULL, '421181', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1799, '2020-03-29 14:20:36.000', 1, 'system.region.area.421182', '武穴市', NULL, '421182', 30, NULL, 1, 16, 1789, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1800, '2020-03-29 14:20:36.000', 1, 'system.region.city.421200', '咸宁市', 'area', '421200', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1801, '2020-03-29 14:20:36.000', 1, 'system.region.area.421202', '咸安区', NULL, '421202', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1802, '2020-03-29 14:20:36.000', 1, 'system.region.area.421221', '嘉鱼县', NULL, '421221', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1803, '2020-03-29 14:20:36.000', 1, 'system.region.area.421222', '通城县', NULL, '421222', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1804, '2020-03-29 14:20:36.000', 1, 'system.region.area.421223', '崇阳县', NULL, '421223', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1805, '2020-03-29 14:20:36.000', 1, 'system.region.area.421224', '通山县', NULL, '421224', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1806, '2020-03-29 14:20:36.000', 1, 'system.region.area.421281', '赤壁市', NULL, '421281', 30, NULL, 1, 16, 1800, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1807, '2020-03-29 14:20:36.000', 1, 'system.region.city.421300', '随州市', 'area', '421300', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1808, '2020-03-29 14:20:36.000', 1, 'system.region.area.421303', '曾都区', NULL, '421303', 30, NULL, 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1809, '2020-03-29 14:20:36.000', 1, 'system.region.area.421321', '随县', NULL, '421321', 30, NULL, 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1810, '2020-03-29 14:20:36.000', 1, 'system.region.area.421381', '广水市', NULL, '421381', 30, NULL, 1, 16, 1807, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1811, '2020-03-29 14:20:36.000', 1, 'system.region.city.422800', '恩施土家族苗族自治州', 'area', '422800', 30, NULL, 1, 15, 1707, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1812, '2020-03-29 14:20:36.000', 1, 'system.region.area.422801', '恩施市', NULL, '422801', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1813, '2020-03-29 14:20:36.000', 1, 'system.region.area.422802', '利川市', NULL, '422802', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1814, '2020-03-29 14:20:36.000', 1, 'system.region.area.422822', '建始县', NULL, '422822', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1815, '2020-03-29 14:20:36.000', 1, 'system.region.area.422823', '巴东县', NULL, '422823', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1816, '2020-03-29 14:20:36.000', 1, 'system.region.area.422825', '宣恩县', NULL, '422825', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1817, '2020-03-29 14:20:36.000', 1, 'system.region.area.422826', '咸丰县', NULL, '422826', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1818, '2020-03-29 14:20:36.000', 1, 'system.region.area.422827', '来凤县', NULL, '422827', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1819, '2020-03-29 14:20:36.000', 1, 'system.region.area.422828', '鹤峰县', NULL, '422828', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1820, '2020-03-29 14:20:36.000', 1, 'system.region.area.429004', '仙桃市', NULL, '429004', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1821, '2020-03-29 14:20:36.000', 1, 'system.region.area.429005', '潜江市', NULL, '429005', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1822, '2020-03-29 14:20:36.000', 1, 'system.region.area.429006', '天门市', NULL, '429006', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1823, '2020-03-29 14:20:36.000', 1, 'system.region.area.429021', '神农架林区', NULL, '429021', 30, NULL, 1, 16, 1811, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1824, '2020-03-29 14:20:36.000', 1, 'system.region.province.430000', '湖南省', 'city', '430000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1825, '2020-03-29 14:20:36.000', 1, 'system.region.city.430100', '长沙市', 'area', '430100', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1826, '2020-03-29 14:20:36.000', 1, 'system.region.area.430102', '芙蓉区', NULL, '430102', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1827, '2020-03-29 14:20:36.000', 1, 'system.region.area.430103', '天心区', NULL, '430103', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1828, '2020-03-29 14:20:36.000', 1, 'system.region.area.430104', '岳麓区', NULL, '430104', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1829, '2020-03-29 14:20:36.000', 1, 'system.region.area.430105', '开福区', NULL, '430105', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1830, '2020-03-29 14:20:36.000', 1, 'system.region.area.430111', '雨花区', NULL, '430111', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1831, '2020-03-29 14:20:36.000', 1, 'system.region.area.430112', '望城区', NULL, '430112', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1832, '2020-03-29 14:20:36.000', 1, 'system.region.area.430121', '长沙县', NULL, '430121', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1833, '2020-03-29 14:20:36.000', 1, 'system.region.area.430181', '浏阳市', NULL, '430181', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1834, '2020-03-29 14:20:36.000', 1, 'system.region.area.430182', '宁乡市', NULL, '430182', 30, NULL, 1, 16, 1825, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1835, '2020-03-29 14:20:36.000', 1, 'system.region.city.430200', '株洲市', 'area', '430200', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1836, '2020-03-29 14:20:36.000', 1, 'system.region.area.430202', '荷塘区', NULL, '430202', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1837, '2020-03-29 14:20:36.000', 1, 'system.region.area.430203', '芦淞区', NULL, '430203', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1838, '2020-03-29 14:20:36.000', 1, 'system.region.area.430204', '石峰区', NULL, '430204', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1839, '2020-03-29 14:20:36.000', 1, 'system.region.area.430211', '天元区', NULL, '430211', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1840, '2020-03-29 14:20:36.000', 1, 'system.region.area.430212', '渌口区', NULL, '430212', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1841, '2020-03-29 14:20:36.000', 1, 'system.region.area.430223', '攸县', NULL, '430223', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1842, '2020-03-29 14:20:36.000', 1, 'system.region.area.430224', '茶陵县', NULL, '430224', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1843, '2020-03-29 14:20:36.000', 1, 'system.region.area.430225', '炎陵县', NULL, '430225', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1844, '2020-03-29 14:20:36.000', 1, 'system.region.area.430281', '醴陵市', NULL, '430281', 30, NULL, 1, 16, 1835, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1845, '2020-03-29 14:20:36.000', 1, 'system.region.city.430300', '湘潭市', 'area', '430300', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1846, '2020-03-29 14:20:36.000', 1, 'system.region.area.430302', '雨湖区', NULL, '430302', 30, NULL, 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1847, '2020-03-29 14:20:36.000', 1, 'system.region.area.430304', '岳塘区', NULL, '430304', 30, NULL, 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1848, '2020-03-29 14:20:36.000', 1, 'system.region.area.430321', '湘潭县', NULL, '430321', 30, NULL, 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1849, '2020-03-29 14:20:36.000', 1, 'system.region.area.430381', '湘乡市', NULL, '430381', 30, NULL, 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1850, '2020-03-29 14:20:36.000', 1, 'system.region.area.430382', '韶山市', NULL, '430382', 30, NULL, 1, 16, 1845, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1851, '2020-03-29 14:20:36.000', 1, 'system.region.city.430400', '衡阳市', 'area', '430400', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1852, '2020-03-29 14:20:36.000', 1, 'system.region.area.430405', '珠晖区', NULL, '430405', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1853, '2020-03-29 14:20:36.000', 1, 'system.region.area.430406', '雁峰区', NULL, '430406', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1854, '2020-03-29 14:20:36.000', 1, 'system.region.area.430407', '石鼓区', NULL, '430407', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1855, '2020-03-29 14:20:36.000', 1, 'system.region.area.430408', '蒸湘区', NULL, '430408', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1856, '2020-03-29 14:20:36.000', 1, 'system.region.area.430412', '南岳区', NULL, '430412', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1857, '2020-03-29 14:20:36.000', 1, 'system.region.area.430421', '衡阳县', NULL, '430421', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1858, '2020-03-29 14:20:36.000', 1, 'system.region.area.430422', '衡南县', NULL, '430422', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1859, '2020-03-29 14:20:36.000', 1, 'system.region.area.430423', '衡山县', NULL, '430423', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1860, '2020-03-29 14:20:36.000', 1, 'system.region.area.430424', '衡东县', NULL, '430424', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1861, '2020-03-29 14:20:36.000', 1, 'system.region.area.430426', '祁东县', NULL, '430426', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1862, '2020-03-29 14:20:36.000', 1, 'system.region.area.430481', '耒阳市', NULL, '430481', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1863, '2020-03-29 14:20:36.000', 1, 'system.region.area.430482', '常宁市', NULL, '430482', 30, NULL, 1, 16, 1851, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1864, '2020-03-29 14:20:36.000', 1, 'system.region.city.430500', '邵阳市', 'area', '430500', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1865, '2020-03-29 14:20:36.000', 1, 'system.region.area.430502', '双清区', NULL, '430502', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1866, '2020-03-29 14:20:36.000', 1, 'system.region.area.430503', '大祥区', NULL, '430503', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1867, '2020-03-29 14:20:36.000', 1, 'system.region.area.430511', '北塔区', NULL, '430511', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1868, '2020-03-29 14:20:36.000', 1, 'system.region.area.430522', '新邵县', NULL, '430522', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1869, '2020-03-29 14:20:36.000', 1, 'system.region.area.430523', '邵阳县', NULL, '430523', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1870, '2020-03-29 14:20:36.000', 1, 'system.region.area.430524', '隆回县', NULL, '430524', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1871, '2020-03-29 14:20:36.000', 1, 'system.region.area.430525', '洞口县', NULL, '430525', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1872, '2020-03-29 14:20:36.000', 1, 'system.region.area.430527', '绥宁县', NULL, '430527', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1873, '2020-03-29 14:20:36.000', 1, 'system.region.area.430528', '新宁县', NULL, '430528', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1874, '2020-03-29 14:20:36.000', 1, 'system.region.area.430529', '城步苗族自治县', NULL, '430529', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1875, '2020-03-29 14:20:36.000', 1, 'system.region.area.430581', '武冈市', NULL, '430581', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1876, '2020-03-29 14:20:36.000', 1, 'system.region.area.430582', '邵东市', NULL, '430582', 30, NULL, 1, 16, 1864, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1877, '2020-03-29 14:20:36.000', 1, 'system.region.city.430600', '岳阳市', 'area', '430600', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1878, '2020-03-29 14:20:36.000', 1, 'system.region.area.430602', '岳阳楼区', NULL, '430602', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1879, '2020-03-29 14:20:36.000', 1, 'system.region.area.430603', '云溪区', NULL, '430603', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1880, '2020-03-29 14:20:36.000', 1, 'system.region.area.430611', '君山区', NULL, '430611', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1881, '2020-03-29 14:20:36.000', 1, 'system.region.area.430621', '岳阳县', NULL, '430621', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1882, '2020-03-29 14:20:36.000', 1, 'system.region.area.430623', '华容县', NULL, '430623', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1883, '2020-03-29 14:20:36.000', 1, 'system.region.area.430624', '湘阴县', NULL, '430624', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1884, '2020-03-29 14:20:36.000', 1, 'system.region.area.430626', '平江县', NULL, '430626', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1885, '2020-03-29 14:20:36.000', 1, 'system.region.area.430681', '汨罗市', NULL, '430681', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1886, '2020-03-29 14:20:36.000', 1, 'system.region.area.430682', '临湘市', NULL, '430682', 30, NULL, 1, 16, 1877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1887, '2020-03-29 14:20:36.000', 1, 'system.region.city.430700', '常德市', 'area', '430700', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1888, '2020-03-29 14:20:36.000', 1, 'system.region.area.430702', '武陵区', NULL, '430702', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1889, '2020-03-29 14:20:36.000', 1, 'system.region.area.430703', '鼎城区', NULL, '430703', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1890, '2020-03-29 14:20:36.000', 1, 'system.region.area.430721', '安乡县', NULL, '430721', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1891, '2020-03-29 14:20:36.000', 1, 'system.region.area.430722', '汉寿县', NULL, '430722', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1892, '2020-03-29 14:20:36.000', 1, 'system.region.area.430723', '澧县', NULL, '430723', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1893, '2020-03-29 14:20:36.000', 1, 'system.region.area.430724', '临澧县', NULL, '430724', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1894, '2020-03-29 14:20:36.000', 1, 'system.region.area.430725', '桃源县', NULL, '430725', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1895, '2020-03-29 14:20:36.000', 1, 'system.region.area.430726', '石门县', NULL, '430726', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1896, '2020-03-29 14:20:36.000', 1, 'system.region.area.430781', '津市市', NULL, '430781', 30, NULL, 1, 16, 1887, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1897, '2020-03-29 14:20:36.000', 1, 'system.region.city.430800', '张家界市', 'area', '430800', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1898, '2020-03-29 14:20:36.000', 1, 'system.region.area.430802', '永定区', NULL, '430802', 30, NULL, 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1899, '2020-03-29 14:20:36.000', 1, 'system.region.area.430811', '武陵源区', NULL, '430811', 30, NULL, 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1900, '2020-03-29 14:20:36.000', 1, 'system.region.area.430821', '慈利县', NULL, '430821', 30, NULL, 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1901, '2020-03-29 14:20:36.000', 1, 'system.region.area.430822', '桑植县', NULL, '430822', 30, NULL, 1, 16, 1897, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1902, '2020-03-29 14:20:36.000', 1, 'system.region.city.430900', '益阳市', 'area', '430900', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1903, '2020-03-29 14:20:36.000', 1, 'system.region.area.430902', '资阳区', NULL, '430902', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1904, '2020-03-29 14:20:36.000', 1, 'system.region.area.430903', '赫山区', NULL, '430903', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1905, '2020-03-29 14:20:36.000', 1, 'system.region.area.430921', '南县', NULL, '430921', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1906, '2020-03-29 14:20:36.000', 1, 'system.region.area.430922', '桃江县', NULL, '430922', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1907, '2020-03-29 14:20:36.000', 1, 'system.region.area.430923', '安化县', NULL, '430923', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1908, '2020-03-29 14:20:36.000', 1, 'system.region.area.430981', '沅江市', NULL, '430981', 30, NULL, 1, 16, 1902, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1909, '2020-03-29 14:20:36.000', 1, 'system.region.city.431000', '郴州市', 'area', '431000', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1910, '2020-03-29 14:20:36.000', 1, 'system.region.area.431002', '北湖区', NULL, '431002', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1911, '2020-03-29 14:20:36.000', 1, 'system.region.area.431003', '苏仙区', NULL, '431003', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1912, '2020-03-29 14:20:36.000', 1, 'system.region.area.431021', '桂阳县', NULL, '431021', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1913, '2020-03-29 14:20:36.000', 1, 'system.region.area.431022', '宜章县', NULL, '431022', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1914, '2020-03-29 14:20:36.000', 1, 'system.region.area.431023', '永兴县', NULL, '431023', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1915, '2020-03-29 14:20:36.000', 1, 'system.region.area.431024', '嘉禾县', NULL, '431024', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1916, '2020-03-29 14:20:36.000', 1, 'system.region.area.431025', '临武县', NULL, '431025', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1917, '2020-03-29 14:20:36.000', 1, 'system.region.area.431026', '汝城县', NULL, '431026', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1918, '2020-03-29 14:20:36.000', 1, 'system.region.area.431027', '桂东县', NULL, '431027', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1919, '2020-03-29 14:20:36.000', 1, 'system.region.area.431028', '安仁县', NULL, '431028', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1920, '2020-03-29 14:20:36.000', 1, 'system.region.area.431081', '资兴市', NULL, '431081', 30, NULL, 1, 16, 1909, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1921, '2020-03-29 14:20:36.000', 1, 'system.region.city.431100', '永州市', 'area', '431100', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1922, '2020-03-29 14:20:36.000', 1, 'system.region.area.431102', '零陵区', NULL, '431102', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1923, '2020-03-29 14:20:36.000', 1, 'system.region.area.431103', '冷水滩区', NULL, '431103', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1924, '2020-03-29 14:20:36.000', 1, 'system.region.area.431121', '祁阳县', NULL, '431121', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1925, '2020-03-29 14:20:36.000', 1, 'system.region.area.431122', '东安县', NULL, '431122', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1926, '2020-03-29 14:20:36.000', 1, 'system.region.area.431123', '双牌县', NULL, '431123', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1927, '2020-03-29 14:20:36.000', 1, 'system.region.area.431124', '道县', NULL, '431124', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1928, '2020-03-29 14:20:36.000', 1, 'system.region.area.431125', '江永县', NULL, '431125', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1929, '2020-03-29 14:20:36.000', 1, 'system.region.area.431126', '宁远县', NULL, '431126', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1930, '2020-03-29 14:20:36.000', 1, 'system.region.area.431127', '蓝山县', NULL, '431127', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1931, '2020-03-29 14:20:36.000', 1, 'system.region.area.431128', '新田县', NULL, '431128', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1932, '2020-03-29 14:20:36.000', 1, 'system.region.area.431129', '江华瑶族自治县', NULL, '431129', 30, NULL, 1, 16, 1921, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1933, '2020-03-29 14:20:36.000', 1, 'system.region.city.431200', '怀化市', 'area', '431200', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1934, '2020-03-29 14:20:36.000', 1, 'system.region.area.431202', '鹤城区', NULL, '431202', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1935, '2020-03-29 14:20:36.000', 1, 'system.region.area.431221', '中方县', NULL, '431221', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1936, '2020-03-29 14:20:36.000', 1, 'system.region.area.431222', '沅陵县', NULL, '431222', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1937, '2020-03-29 14:20:36.000', 1, 'system.region.area.431223', '辰溪县', NULL, '431223', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1938, '2020-03-29 14:20:36.000', 1, 'system.region.area.431224', '溆浦县', NULL, '431224', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1939, '2020-03-29 14:20:36.000', 1, 'system.region.area.431225', '会同县', NULL, '431225', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1940, '2020-03-29 14:20:36.000', 1, 'system.region.area.431226', '麻阳苗族自治县', NULL, '431226', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1941, '2020-03-29 14:20:36.000', 1, 'system.region.area.431227', '新晃侗族自治县', NULL, '431227', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1942, '2020-03-29 14:20:36.000', 1, 'system.region.area.431228', '芷江侗族自治县', NULL, '431228', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1943, '2020-03-29 14:20:36.000', 1, 'system.region.area.431229', '靖州苗族侗族自治县', NULL, '431229', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1944, '2020-03-29 14:20:36.000', 1, 'system.region.area.431230', '通道侗族自治县', NULL, '431230', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1945, '2020-03-29 14:20:36.000', 1, 'system.region.area.431281', '洪江市', NULL, '431281', 30, NULL, 1, 16, 1933, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1946, '2020-03-29 14:20:36.000', 1, 'system.region.city.431300', '娄底市', 'area', '431300', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1947, '2020-03-29 14:20:36.000', 1, 'system.region.area.431302', '娄星区', NULL, '431302', 30, NULL, 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1948, '2020-03-29 14:20:36.000', 1, 'system.region.area.431321', '双峰县', NULL, '431321', 30, NULL, 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1949, '2020-03-29 14:20:36.000', 1, 'system.region.area.431322', '新化县', NULL, '431322', 30, NULL, 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1950, '2020-03-29 14:20:36.000', 1, 'system.region.area.431381', '冷水江市', NULL, '431381', 30, NULL, 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1951, '2020-03-29 14:20:36.000', 1, 'system.region.area.431382', '涟源市', NULL, '431382', 30, NULL, 1, 16, 1946, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1952, '2020-03-29 14:20:36.000', 1, 'system.region.city.433100', '湘西土家族苗族自治州', 'area', '433100', 30, NULL, 1, 15, 1824, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1953, '2020-03-29 14:20:36.000', 1, 'system.region.area.433101', '吉首市', NULL, '433101', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1954, '2020-03-29 14:20:36.000', 1, 'system.region.area.433122', '泸溪县', NULL, '433122', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1955, '2020-03-29 14:20:36.000', 1, 'system.region.area.433123', '凤凰县', NULL, '433123', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1956, '2020-03-29 14:20:36.000', 1, 'system.region.area.433124', '花垣县', NULL, '433124', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1957, '2020-03-29 14:20:36.000', 1, 'system.region.area.433125', '保靖县', NULL, '433125', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1958, '2020-03-29 14:20:36.000', 1, 'system.region.area.433126', '古丈县', NULL, '433126', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1959, '2020-03-29 14:20:36.000', 1, 'system.region.area.433127', '永顺县', NULL, '433127', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1960, '2020-03-29 14:20:36.000', 1, 'system.region.area.433130', '龙山县', NULL, '433130', 30, NULL, 1, 16, 1952, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1961, '2020-03-29 14:20:36.000', 1, 'system.region.province.440000', '广东省', 'city', '440000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1962, '2020-03-29 14:20:36.000', 1, 'system.region.city.440100', '广州市', 'area', '440100', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1963, '2020-03-29 14:20:36.000', 1, 'system.region.area.440103', '荔湾区', NULL, '440103', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1964, '2020-03-29 14:20:36.000', 1, 'system.region.area.440104', '越秀区', NULL, '440104', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1965, '2020-03-29 14:20:36.000', 1, 'system.region.area.440105', '海珠区', NULL, '440105', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1966, '2020-03-29 14:20:36.000', 1, 'system.region.area.440106', '天河区', NULL, '440106', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1967, '2020-03-29 14:20:36.000', 1, 'system.region.area.440111', '白云区', NULL, '440111', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1968, '2020-03-29 14:20:36.000', 1, 'system.region.area.440112', '黄埔区', NULL, '440112', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1969, '2020-03-29 14:20:36.000', 1, 'system.region.area.440113', '番禺区', NULL, '440113', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1970, '2020-03-29 14:20:36.000', 1, 'system.region.area.440114', '花都区', NULL, '440114', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1971, '2020-03-29 14:20:36.000', 1, 'system.region.area.440115', '南沙区', NULL, '440115', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1972, '2020-03-29 14:20:36.000', 1, 'system.region.area.440117', '从化区', NULL, '440117', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1973, '2020-03-29 14:20:36.000', 1, 'system.region.area.440118', '增城区', NULL, '440118', 30, NULL, 1, 16, 1962, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1974, '2020-03-29 14:20:36.000', 1, 'system.region.city.440200', '韶关市', 'area', '440200', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1975, '2020-03-29 14:20:36.000', 1, 'system.region.area.440203', '武江区', NULL, '440203', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1976, '2020-03-29 14:20:36.000', 1, 'system.region.area.440204', '浈江区', NULL, '440204', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1977, '2020-03-29 14:20:36.000', 1, 'system.region.area.440205', '曲江区', NULL, '440205', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1978, '2020-03-29 14:20:36.000', 1, 'system.region.area.440222', '始兴县', NULL, '440222', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1979, '2020-03-29 14:20:36.000', 1, 'system.region.area.440224', '仁化县', NULL, '440224', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1980, '2020-03-29 14:20:36.000', 1, 'system.region.area.440229', '翁源县', NULL, '440229', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1981, '2020-03-29 14:20:36.000', 1, 'system.region.area.440232', '乳源瑶族自治县', NULL, '440232', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1982, '2020-03-29 14:20:36.000', 1, 'system.region.area.440233', '新丰县', NULL, '440233', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1983, '2020-03-29 14:20:36.000', 1, 'system.region.area.440281', '乐昌市', NULL, '440281', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1984, '2020-03-29 14:20:36.000', 1, 'system.region.area.440282', '南雄市', NULL, '440282', 30, NULL, 1, 16, 1974, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1985, '2020-03-29 14:20:36.000', 1, 'system.region.city.440300', '深圳市', 'area', '440300', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1986, '2020-03-29 14:20:36.000', 1, 'system.region.area.440303', '罗湖区', NULL, '440303', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1987, '2020-03-29 14:20:36.000', 1, 'system.region.area.440304', '福田区', NULL, '440304', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1988, '2020-03-29 14:20:36.000', 1, 'system.region.area.440305', '南山区', NULL, '440305', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1989, '2020-03-29 14:20:36.000', 1, 'system.region.area.440306', '宝安区', NULL, '440306', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1990, '2020-03-29 14:20:36.000', 1, 'system.region.area.440307', '龙岗区', NULL, '440307', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1991, '2020-03-29 14:20:36.000', 1, 'system.region.area.440308', '盐田区', NULL, '440308', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1992, '2020-03-29 14:20:36.000', 1, 'system.region.area.440309', '龙华区', NULL, '440309', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1993, '2020-03-29 14:20:36.000', 1, 'system.region.area.440310', '坪山区', NULL, '440310', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1994, '2020-03-29 14:20:36.000', 1, 'system.region.area.440311', '光明区', NULL, '440311', 30, NULL, 1, 16, 1985, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1995, '2020-03-29 14:20:36.000', 1, 'system.region.city.440400', '珠海市', 'area', '440400', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1996, '2020-03-29 14:20:36.000', 1, 'system.region.area.440402', '香洲区', NULL, '440402', 30, NULL, 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1997, '2020-03-29 14:20:36.000', 1, 'system.region.area.440403', '斗门区', NULL, '440403', 30, NULL, 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1998, '2020-03-29 14:20:36.000', 1, 'system.region.area.440404', '金湾区', NULL, '440404', 30, NULL, 1, 16, 1995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (1999, '2020-03-29 14:20:36.000', 1, 'system.region.city.440500', '汕头市', 'area', '440500', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2000, '2020-03-29 14:20:36.000', 1, 'system.region.area.440507', '龙湖区', NULL, '440507', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2001, '2020-03-29 14:20:36.000', 1, 'system.region.area.440511', '金平区', NULL, '440511', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2002, '2020-03-29 14:20:36.000', 1, 'system.region.area.440512', '濠江区', NULL, '440512', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2003, '2020-03-29 14:20:36.000', 1, 'system.region.area.440513', '潮阳区', NULL, '440513', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2004, '2020-03-29 14:20:36.000', 1, 'system.region.area.440514', '潮南区', NULL, '440514', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2005, '2020-03-29 14:20:36.000', 1, 'system.region.area.440515', '澄海区', NULL, '440515', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2006, '2020-03-29 14:20:36.000', 1, 'system.region.area.440523', '南澳县', NULL, '440523', 30, NULL, 1, 16, 1999, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2007, '2020-03-29 14:20:36.000', 1, 'system.region.city.440600', '佛山市', 'area', '440600', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2008, '2020-03-29 14:20:36.000', 1, 'system.region.area.440604', '禅城区', NULL, '440604', 30, NULL, 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2009, '2020-03-29 14:20:36.000', 1, 'system.region.area.440605', '南海区', NULL, '440605', 30, NULL, 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2010, '2020-03-29 14:20:36.000', 1, 'system.region.area.440606', '顺德区', NULL, '440606', 30, NULL, 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2011, '2020-03-29 14:20:36.000', 1, 'system.region.area.440607', '三水区', NULL, '440607', 30, NULL, 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2012, '2020-03-29 14:20:36.000', 1, 'system.region.area.440608', '高明区', NULL, '440608', 30, NULL, 1, 16, 2007, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2013, '2020-03-29 14:20:36.000', 1, 'system.region.city.440700', '江门市', 'area', '440700', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2014, '2020-03-29 14:20:36.000', 1, 'system.region.area.440703', '蓬江区', NULL, '440703', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2015, '2020-03-29 14:20:36.000', 1, 'system.region.area.440704', '江海区', NULL, '440704', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2016, '2020-03-29 14:20:36.000', 1, 'system.region.area.440705', '新会区', NULL, '440705', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2017, '2020-03-29 14:20:36.000', 1, 'system.region.area.440781', '台山市', NULL, '440781', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2018, '2020-03-29 14:20:36.000', 1, 'system.region.area.440783', '开平市', NULL, '440783', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2019, '2020-03-29 14:20:36.000', 1, 'system.region.area.440784', '鹤山市', NULL, '440784', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2020, '2020-03-29 14:20:36.000', 1, 'system.region.area.440785', '恩平市', NULL, '440785', 30, NULL, 1, 16, 2013, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2021, '2020-03-29 14:20:36.000', 1, 'system.region.city.440800', '湛江市', 'area', '440800', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2022, '2020-03-29 14:20:36.000', 1, 'system.region.area.440802', '赤坎区', NULL, '440802', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2023, '2020-03-29 14:20:36.000', 1, 'system.region.area.440803', '霞山区', NULL, '440803', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2024, '2020-03-29 14:20:36.000', 1, 'system.region.area.440804', '坡头区', NULL, '440804', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2025, '2020-03-29 14:20:36.000', 1, 'system.region.area.440811', '麻章区', NULL, '440811', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2026, '2020-03-29 14:20:36.000', 1, 'system.region.area.440823', '遂溪县', NULL, '440823', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2027, '2020-03-29 14:20:36.000', 1, 'system.region.area.440825', '徐闻县', NULL, '440825', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2028, '2020-03-29 14:20:36.000', 1, 'system.region.area.440881', '廉江市', NULL, '440881', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2029, '2020-03-29 14:20:36.000', 1, 'system.region.area.440882', '雷州市', NULL, '440882', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2030, '2020-03-29 14:20:36.000', 1, 'system.region.area.440883', '吴川市', NULL, '440883', 30, NULL, 1, 16, 2021, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2031, '2020-03-29 14:20:36.000', 1, 'system.region.city.440900', '茂名市', 'area', '440900', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2032, '2020-03-29 14:20:36.000', 1, 'system.region.area.440902', '茂南区', NULL, '440902', 30, NULL, 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2033, '2020-03-29 14:20:36.000', 1, 'system.region.area.440904', '电白区', NULL, '440904', 30, NULL, 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2034, '2020-03-29 14:20:36.000', 1, 'system.region.area.440981', '高州市', NULL, '440981', 30, NULL, 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2035, '2020-03-29 14:20:36.000', 1, 'system.region.area.440982', '化州市', NULL, '440982', 30, NULL, 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2036, '2020-03-29 14:20:36.000', 1, 'system.region.area.440983', '信宜市', NULL, '440983', 30, NULL, 1, 16, 2031, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2037, '2020-03-29 14:20:36.000', 1, 'system.region.city.441200', '肇庆市', 'area', '441200', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2038, '2020-03-29 14:20:36.000', 1, 'system.region.area.441202', '端州区', NULL, '441202', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2039, '2020-03-29 14:20:36.000', 1, 'system.region.area.441203', '鼎湖区', NULL, '441203', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2040, '2020-03-29 14:20:36.000', 1, 'system.region.area.441204', '高要区', NULL, '441204', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2041, '2020-03-29 14:20:36.000', 1, 'system.region.area.441223', '广宁县', NULL, '441223', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2042, '2020-03-29 14:20:36.000', 1, 'system.region.area.441224', '怀集县', NULL, '441224', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2043, '2020-03-29 14:20:36.000', 1, 'system.region.area.441225', '封开县', NULL, '441225', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2044, '2020-03-29 14:20:36.000', 1, 'system.region.area.441226', '德庆县', NULL, '441226', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2045, '2020-03-29 14:20:36.000', 1, 'system.region.area.441284', '四会市', NULL, '441284', 30, NULL, 1, 16, 2037, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2046, '2020-03-29 14:20:36.000', 1, 'system.region.city.441300', '惠州市', 'area', '441300', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2047, '2020-03-29 14:20:36.000', 1, 'system.region.area.441302', '惠城区', NULL, '441302', 30, NULL, 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2048, '2020-03-29 14:20:36.000', 1, 'system.region.area.441303', '惠阳区', NULL, '441303', 30, NULL, 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2049, '2020-03-29 14:20:36.000', 1, 'system.region.area.441322', '博罗县', NULL, '441322', 30, NULL, 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2050, '2020-03-29 14:20:36.000', 1, 'system.region.area.441323', '惠东县', NULL, '441323', 30, NULL, 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2051, '2020-03-29 14:20:36.000', 1, 'system.region.area.441324', '龙门县', NULL, '441324', 30, NULL, 1, 16, 2046, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2052, '2020-03-29 14:20:36.000', 1, 'system.region.city.441400', '梅州市', 'area', '441400', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2053, '2020-03-29 14:20:36.000', 1, 'system.region.area.441402', '梅江区', NULL, '441402', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2054, '2020-03-29 14:20:36.000', 1, 'system.region.area.441403', '梅县区', NULL, '441403', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2055, '2020-03-29 14:20:36.000', 1, 'system.region.area.441422', '大埔县', NULL, '441422', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2056, '2020-03-29 14:20:36.000', 1, 'system.region.area.441423', '丰顺县', NULL, '441423', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2057, '2020-03-29 14:20:36.000', 1, 'system.region.area.441424', '五华县', NULL, '441424', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2058, '2020-03-29 14:20:36.000', 1, 'system.region.area.441426', '平远县', NULL, '441426', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2059, '2020-03-29 14:20:36.000', 1, 'system.region.area.441427', '蕉岭县', NULL, '441427', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2060, '2020-03-29 14:20:36.000', 1, 'system.region.area.441481', '兴宁市', NULL, '441481', 30, NULL, 1, 16, 2052, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2061, '2020-03-29 14:20:36.000', 1, 'system.region.city.441500', '汕尾市', 'area', '441500', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2062, '2020-03-29 14:20:36.000', 1, 'system.region.area.441502', '城区', NULL, '441502', 30, NULL, 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2063, '2020-03-29 14:20:36.000', 1, 'system.region.area.441521', '海丰县', NULL, '441521', 30, NULL, 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2064, '2020-03-29 14:20:36.000', 1, 'system.region.area.441523', '陆河县', NULL, '441523', 30, NULL, 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2065, '2020-03-29 14:20:36.000', 1, 'system.region.area.441581', '陆丰市', NULL, '441581', 30, NULL, 1, 16, 2061, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2066, '2020-03-29 14:20:36.000', 1, 'system.region.city.441600', '河源市', 'area', '441600', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2067, '2020-03-29 14:20:36.000', 1, 'system.region.area.441602', '源城区', NULL, '441602', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2068, '2020-03-29 14:20:36.000', 1, 'system.region.area.441621', '紫金县', NULL, '441621', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2069, '2020-03-29 14:20:36.000', 1, 'system.region.area.441622', '龙川县', NULL, '441622', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2070, '2020-03-29 14:20:36.000', 1, 'system.region.area.441623', '连平县', NULL, '441623', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2071, '2020-03-29 14:20:36.000', 1, 'system.region.area.441624', '和平县', NULL, '441624', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2072, '2020-03-29 14:20:36.000', 1, 'system.region.area.441625', '东源县', NULL, '441625', 30, NULL, 1, 16, 2066, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2073, '2020-03-29 14:20:36.000', 1, 'system.region.city.441700', '阳江市', 'area', '441700', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2074, '2020-03-29 14:20:36.000', 1, 'system.region.area.441702', '江城区', NULL, '441702', 30, NULL, 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2075, '2020-03-29 14:20:36.000', 1, 'system.region.area.441704', '阳东区', NULL, '441704', 30, NULL, 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2076, '2020-03-29 14:20:36.000', 1, 'system.region.area.441721', '阳西县', NULL, '441721', 30, NULL, 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2077, '2020-03-29 14:20:36.000', 1, 'system.region.area.441781', '阳春市', NULL, '441781', 30, NULL, 1, 16, 2073, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2078, '2020-03-29 14:20:36.000', 1, 'system.region.city.441800', '清远市', 'area', '441800', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2079, '2020-03-29 14:20:36.000', 1, 'system.region.area.441802', '清城区', NULL, '441802', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2080, '2020-03-29 14:20:36.000', 1, 'system.region.area.441803', '清新区', NULL, '441803', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2081, '2020-03-29 14:20:36.000', 1, 'system.region.area.441821', '佛冈县', NULL, '441821', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2082, '2020-03-29 14:20:36.000', 1, 'system.region.area.441823', '阳山县', NULL, '441823', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2083, '2020-03-29 14:20:36.000', 1, 'system.region.area.441825', '连山壮族瑶族自治县', NULL, '441825', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2084, '2020-03-29 14:20:36.000', 1, 'system.region.area.441826', '连南瑶族自治县', NULL, '441826', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2085, '2020-03-29 14:20:36.000', 1, 'system.region.area.441881', '英德市', NULL, '441881', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2086, '2020-03-29 14:20:36.000', 1, 'system.region.area.441882', '连州市', NULL, '441882', 30, NULL, 1, 16, 2078, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2087, '2020-03-29 14:20:36.000', 1, 'system.region.city.441900', '东莞市', 'area', '441900', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2088, '2020-03-29 14:20:36.000', 1, 'system.region.city.442000', '中山市', 'area', '442000', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2089, '2020-03-29 14:20:36.000', 1, 'system.region.city.445100', '潮州市', 'area', '445100', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2090, '2020-03-29 14:20:36.000', 1, 'system.region.area.445102', '湘桥区', NULL, '445102', 30, NULL, 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2091, '2020-03-29 14:20:36.000', 1, 'system.region.area.445103', '潮安区', NULL, '445103', 30, NULL, 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2092, '2020-03-29 14:20:36.000', 1, 'system.region.area.445122', '饶平县', NULL, '445122', 30, NULL, 1, 16, 2089, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2093, '2020-03-29 14:20:36.000', 1, 'system.region.city.445200', '揭阳市', 'area', '445200', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2094, '2020-03-29 14:20:36.000', 1, 'system.region.area.445202', '榕城区', NULL, '445202', 30, NULL, 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2095, '2020-03-29 14:20:36.000', 1, 'system.region.area.445203', '揭东区', NULL, '445203', 30, NULL, 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2096, '2020-03-29 14:20:36.000', 1, 'system.region.area.445222', '揭西县', NULL, '445222', 30, NULL, 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2097, '2020-03-29 14:20:36.000', 1, 'system.region.area.445224', '惠来县', NULL, '445224', 30, NULL, 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2098, '2020-03-29 14:20:36.000', 1, 'system.region.area.445281', '普宁市', NULL, '445281', 30, NULL, 1, 16, 2093, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2099, '2020-03-29 14:20:36.000', 1, 'system.region.city.445300', '云浮市', 'area', '445300', 30, NULL, 1, 15, 1961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2100, '2020-03-29 14:20:36.000', 1, 'system.region.area.445302', '云城区', NULL, '445302', 30, NULL, 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2101, '2020-03-29 14:20:36.000', 1, 'system.region.area.445303', '云安区', NULL, '445303', 30, NULL, 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2102, '2020-03-29 14:20:36.000', 1, 'system.region.area.445321', '新兴县', NULL, '445321', 30, NULL, 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2103, '2020-03-29 14:20:36.000', 1, 'system.region.area.445322', '郁南县', NULL, '445322', 30, NULL, 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2104, '2020-03-29 14:20:36.000', 1, 'system.region.area.445381', '罗定市', NULL, '445381', 30, NULL, 1, 16, 2099, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2105, '2020-03-29 14:20:36.000', 1, 'system.region.province.450000', '广西壮族自治区', 'city', '450000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2106, '2020-03-29 14:20:36.000', 1, 'system.region.city.450100', '南宁市', 'area', '450100', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2107, '2020-03-29 14:20:36.000', 1, 'system.region.area.450102', '兴宁区', NULL, '450102', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2108, '2020-03-29 14:20:36.000', 1, 'system.region.area.450103', '青秀区', NULL, '450103', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2109, '2020-03-29 14:20:36.000', 1, 'system.region.area.450105', '江南区', NULL, '450105', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2110, '2020-03-29 14:20:36.000', 1, 'system.region.area.450107', '西乡塘区', NULL, '450107', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2111, '2020-03-29 14:20:36.000', 1, 'system.region.area.450108', '良庆区', NULL, '450108', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2112, '2020-03-29 14:20:36.000', 1, 'system.region.area.450109', '邕宁区', NULL, '450109', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2113, '2020-03-29 14:20:36.000', 1, 'system.region.area.450110', '武鸣区', NULL, '450110', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2114, '2020-03-29 14:20:36.000', 1, 'system.region.area.450123', '隆安县', NULL, '450123', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2115, '2020-03-29 14:20:36.000', 1, 'system.region.area.450124', '马山县', NULL, '450124', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2116, '2020-03-29 14:20:36.000', 1, 'system.region.area.450125', '上林县', NULL, '450125', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2117, '2020-03-29 14:20:36.000', 1, 'system.region.area.450126', '宾阳县', NULL, '450126', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2118, '2020-03-29 14:20:36.000', 1, 'system.region.area.450127', '横县', NULL, '450127', 30, NULL, 1, 16, 2106, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2119, '2020-03-29 14:20:36.000', 1, 'system.region.city.450200', '柳州市', 'area', '450200', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2120, '2020-03-29 14:20:36.000', 1, 'system.region.area.450202', '城中区', NULL, '450202', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2121, '2020-03-29 14:20:36.000', 1, 'system.region.area.450203', '鱼峰区', NULL, '450203', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2122, '2020-03-29 14:20:36.000', 1, 'system.region.area.450204', '柳南区', NULL, '450204', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2123, '2020-03-29 14:20:36.000', 1, 'system.region.area.450205', '柳北区', NULL, '450205', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2124, '2020-03-29 14:20:36.000', 1, 'system.region.area.450206', '柳江区', NULL, '450206', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2125, '2020-03-29 14:20:36.000', 1, 'system.region.area.450222', '柳城县', NULL, '450222', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2126, '2020-03-29 14:20:36.000', 1, 'system.region.area.450223', '鹿寨县', NULL, '450223', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2127, '2020-03-29 14:20:36.000', 1, 'system.region.area.450224', '融安县', NULL, '450224', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2128, '2020-03-29 14:20:36.000', 1, 'system.region.area.450225', '融水苗族自治县', NULL, '450225', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2129, '2020-03-29 14:20:36.000', 1, 'system.region.area.450226', '三江侗族自治县', NULL, '450226', 30, NULL, 1, 16, 2119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2130, '2020-03-29 14:20:36.000', 1, 'system.region.city.450300', '桂林市', 'area', '450300', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2131, '2020-03-29 14:20:36.000', 1, 'system.region.area.450302', '秀峰区', NULL, '450302', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2132, '2020-03-29 14:20:36.000', 1, 'system.region.area.450303', '叠彩区', NULL, '450303', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2133, '2020-03-29 14:20:36.000', 1, 'system.region.area.450304', '象山区', NULL, '450304', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2134, '2020-03-29 14:20:36.000', 1, 'system.region.area.450305', '七星区', NULL, '450305', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2135, '2020-03-29 14:20:36.000', 1, 'system.region.area.450311', '雁山区', NULL, '450311', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2136, '2020-03-29 14:20:36.000', 1, 'system.region.area.450312', '临桂区', NULL, '450312', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2137, '2020-03-29 14:20:36.000', 1, 'system.region.area.450321', '阳朔县', NULL, '450321', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2138, '2020-03-29 14:20:36.000', 1, 'system.region.area.450323', '灵川县', NULL, '450323', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2139, '2020-03-29 14:20:36.000', 1, 'system.region.area.450324', '全州县', NULL, '450324', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2140, '2020-03-29 14:20:36.000', 1, 'system.region.area.450325', '兴安县', NULL, '450325', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2141, '2020-03-29 14:20:36.000', 1, 'system.region.area.450326', '永福县', NULL, '450326', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2142, '2020-03-29 14:20:36.000', 1, 'system.region.area.450327', '灌阳县', NULL, '450327', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2143, '2020-03-29 14:20:36.000', 1, 'system.region.area.450328', '龙胜各族自治县', NULL, '450328', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2144, '2020-03-29 14:20:36.000', 1, 'system.region.area.450329', '资源县', NULL, '450329', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2145, '2020-03-29 14:20:36.000', 1, 'system.region.area.450330', '平乐县', NULL, '450330', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2146, '2020-03-29 14:20:36.000', 1, 'system.region.area.450381', '荔浦市', NULL, '450381', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2147, '2020-03-29 14:20:36.000', 1, 'system.region.area.450332', '恭城瑶族自治县', NULL, '450332', 30, NULL, 1, 16, 2130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2148, '2020-03-29 14:20:36.000', 1, 'system.region.city.450400', '梧州市', 'area', '450400', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2149, '2020-03-29 14:20:36.000', 1, 'system.region.area.450403', '万秀区', NULL, '450403', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2150, '2020-03-29 14:20:36.000', 1, 'system.region.area.450405', '长洲区', NULL, '450405', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2151, '2020-03-29 14:20:36.000', 1, 'system.region.area.450406', '龙圩区', NULL, '450406', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2152, '2020-03-29 14:20:36.000', 1, 'system.region.area.450421', '苍梧县', NULL, '450421', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2153, '2020-03-29 14:20:36.000', 1, 'system.region.area.450422', '藤县', NULL, '450422', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2154, '2020-03-29 14:20:36.000', 1, 'system.region.area.450423', '蒙山县', NULL, '450423', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2155, '2020-03-29 14:20:36.000', 1, 'system.region.area.450481', '岑溪市', NULL, '450481', 30, NULL, 1, 16, 2148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2156, '2020-03-29 14:20:36.000', 1, 'system.region.city.450500', '北海市', 'area', '450500', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2157, '2020-03-29 14:20:36.000', 1, 'system.region.area.450502', '海城区', NULL, '450502', 30, NULL, 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2158, '2020-03-29 14:20:36.000', 1, 'system.region.area.450503', '银海区', NULL, '450503', 30, NULL, 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2159, '2020-03-29 14:20:36.000', 1, 'system.region.area.450512', '铁山港区', NULL, '450512', 30, NULL, 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2160, '2020-03-29 14:20:36.000', 1, 'system.region.area.450521', '合浦县', NULL, '450521', 30, NULL, 1, 16, 2156, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2161, '2020-03-29 14:20:36.000', 1, 'system.region.city.450600', '防城港市', 'area', '450600', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2162, '2020-03-29 14:20:36.000', 1, 'system.region.area.450602', '港口区', NULL, '450602', 30, NULL, 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2163, '2020-03-29 14:20:36.000', 1, 'system.region.area.450603', '防城区', NULL, '450603', 30, NULL, 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2164, '2020-03-29 14:20:36.000', 1, 'system.region.area.450621', '上思县', NULL, '450621', 30, NULL, 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2165, '2020-03-29 14:20:36.000', 1, 'system.region.area.450681', '东兴市', NULL, '450681', 30, NULL, 1, 16, 2161, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2166, '2020-03-29 14:20:36.000', 1, 'system.region.city.450700', '钦州市', 'area', '450700', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2167, '2020-03-29 14:20:36.000', 1, 'system.region.area.450702', '钦南区', NULL, '450702', 30, NULL, 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2168, '2020-03-29 14:20:36.000', 1, 'system.region.area.450703', '钦北区', NULL, '450703', 30, NULL, 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2169, '2020-03-29 14:20:36.000', 1, 'system.region.area.450721', '灵山县', NULL, '450721', 30, NULL, 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2170, '2020-03-29 14:20:36.000', 1, 'system.region.area.450722', '浦北县', NULL, '450722', 30, NULL, 1, 16, 2166, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2171, '2020-03-29 14:20:36.000', 1, 'system.region.city.450800', '贵港市', 'area', '450800', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2172, '2020-03-29 14:20:36.000', 1, 'system.region.area.450802', '港北区', NULL, '450802', 30, NULL, 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2173, '2020-03-29 14:20:36.000', 1, 'system.region.area.450803', '港南区', NULL, '450803', 30, NULL, 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2174, '2020-03-29 14:20:36.000', 1, 'system.region.area.450804', '覃塘区', NULL, '450804', 30, NULL, 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2175, '2020-03-29 14:20:36.000', 1, 'system.region.area.450821', '平南县', NULL, '450821', 30, NULL, 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2176, '2020-03-29 14:20:36.000', 1, 'system.region.area.450881', '桂平市', NULL, '450881', 30, NULL, 1, 16, 2171, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2177, '2020-03-29 14:20:36.000', 1, 'system.region.city.450900', '玉林市', 'area', '450900', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2178, '2020-03-29 14:20:36.000', 1, 'system.region.area.450902', '玉州区', NULL, '450902', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2179, '2020-03-29 14:20:36.000', 1, 'system.region.area.450903', '福绵区', NULL, '450903', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2180, '2020-03-29 14:20:36.000', 1, 'system.region.area.450921', '容县', NULL, '450921', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2181, '2020-03-29 14:20:36.000', 1, 'system.region.area.450922', '陆川县', NULL, '450922', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2182, '2020-03-29 14:20:36.000', 1, 'system.region.area.450923', '博白县', NULL, '450923', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2183, '2020-03-29 14:20:36.000', 1, 'system.region.area.450924', '兴业县', NULL, '450924', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2184, '2020-03-29 14:20:36.000', 1, 'system.region.area.450981', '北流市', NULL, '450981', 30, NULL, 1, 16, 2177, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2185, '2020-03-29 14:20:36.000', 1, 'system.region.city.451000', '百色市', 'area', '451000', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2186, '2020-03-29 14:20:36.000', 1, 'system.region.area.451002', '右江区', NULL, '451002', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2187, '2020-03-29 14:20:36.000', 1, 'system.region.area.451003', '田阳区', NULL, '451003', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2188, '2020-03-29 14:20:36.000', 1, 'system.region.area.451022', '田东县', NULL, '451022', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2189, '2020-03-29 14:20:36.000', 1, 'system.region.area.451024', '德保县', NULL, '451024', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2190, '2020-03-29 14:20:36.000', 1, 'system.region.area.451026', '那坡县', NULL, '451026', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2191, '2020-03-29 14:20:36.000', 1, 'system.region.area.451027', '凌云县', NULL, '451027', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2192, '2020-03-29 14:20:36.000', 1, 'system.region.area.451028', '乐业县', NULL, '451028', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2193, '2020-03-29 14:20:36.000', 1, 'system.region.area.451029', '田林县', NULL, '451029', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2194, '2020-03-29 14:20:36.000', 1, 'system.region.area.451030', '西林县', NULL, '451030', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2195, '2020-03-29 14:20:36.000', 1, 'system.region.area.451031', '隆林各族自治县', NULL, '451031', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2196, '2020-03-29 14:20:36.000', 1, 'system.region.area.451081', '靖西市', NULL, '451081', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2197, '2020-03-29 14:20:36.000', 1, 'system.region.area.451082', '平果市', NULL, '451082', 30, NULL, 1, 16, 2185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2198, '2020-03-29 14:20:36.000', 1, 'system.region.city.451100', '贺州市', 'area', '451100', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2199, '2020-03-29 14:20:36.000', 1, 'system.region.area.451102', '八步区', NULL, '451102', 30, NULL, 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2200, '2020-03-29 14:20:36.000', 1, 'system.region.area.451103', '平桂区', NULL, '451103', 30, NULL, 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2201, '2020-03-29 14:20:36.000', 1, 'system.region.area.451121', '昭平县', NULL, '451121', 30, NULL, 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2202, '2020-03-29 14:20:36.000', 1, 'system.region.area.451122', '钟山县', NULL, '451122', 30, NULL, 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2203, '2020-03-29 14:20:36.000', 1, 'system.region.area.451123', '富川瑶族自治县', NULL, '451123', 30, NULL, 1, 16, 2198, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2204, '2020-03-29 14:20:36.000', 1, 'system.region.city.451200', '河池市', 'area', '451200', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2205, '2020-03-29 14:20:36.000', 1, 'system.region.area.451202', '金城江区', NULL, '451202', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2206, '2020-03-29 14:20:36.000', 1, 'system.region.area.451203', '宜州区', NULL, '451203', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2207, '2020-03-29 14:20:36.000', 1, 'system.region.area.451221', '南丹县', NULL, '451221', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2208, '2020-03-29 14:20:36.000', 1, 'system.region.area.451222', '天峨县', NULL, '451222', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2209, '2020-03-29 14:20:36.000', 1, 'system.region.area.451223', '凤山县', NULL, '451223', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2210, '2020-03-29 14:20:36.000', 1, 'system.region.area.451224', '东兰县', NULL, '451224', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2211, '2020-03-29 14:20:36.000', 1, 'system.region.area.451225', '罗城仫佬族自治县', NULL, '451225', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2212, '2020-03-29 14:20:36.000', 1, 'system.region.area.451226', '环江毛南族自治县', NULL, '451226', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2213, '2020-03-29 14:20:36.000', 1, 'system.region.area.451227', '巴马瑶族自治县', NULL, '451227', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2214, '2020-03-29 14:20:36.000', 1, 'system.region.area.451228', '都安瑶族自治县', NULL, '451228', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2215, '2020-03-29 14:20:36.000', 1, 'system.region.area.451229', '大化瑶族自治县', NULL, '451229', 30, NULL, 1, 16, 2204, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2216, '2020-03-29 14:20:36.000', 1, 'system.region.city.451300', '来宾市', 'area', '451300', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2217, '2020-03-29 14:20:36.000', 1, 'system.region.area.451302', '兴宾区', NULL, '451302', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2218, '2020-03-29 14:20:36.000', 1, 'system.region.area.451321', '忻城县', NULL, '451321', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2219, '2020-03-29 14:20:36.000', 1, 'system.region.area.451322', '象州县', NULL, '451322', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2220, '2020-03-29 14:20:36.000', 1, 'system.region.area.451323', '武宣县', NULL, '451323', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2221, '2020-03-29 14:20:36.000', 1, 'system.region.area.451324', '金秀瑶族自治县', NULL, '451324', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2222, '2020-03-29 14:20:36.000', 1, 'system.region.area.451381', '合山市', NULL, '451381', 30, NULL, 1, 16, 2216, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2223, '2020-03-29 14:20:36.000', 1, 'system.region.city.451400', '崇左市', 'area', '451400', 30, NULL, 1, 15, 2105, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2224, '2020-03-29 14:20:36.000', 1, 'system.region.area.451402', '江州区', NULL, '451402', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2225, '2020-03-29 14:20:36.000', 1, 'system.region.area.451421', '扶绥县', NULL, '451421', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2226, '2020-03-29 14:20:36.000', 1, 'system.region.area.451422', '宁明县', NULL, '451422', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2227, '2020-03-29 14:20:36.000', 1, 'system.region.area.451423', '龙州县', NULL, '451423', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2228, '2020-03-29 14:20:36.000', 1, 'system.region.area.451424', '大新县', NULL, '451424', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2229, '2020-03-29 14:20:36.000', 1, 'system.region.area.451425', '天等县', NULL, '451425', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2230, '2020-03-29 14:20:36.000', 1, 'system.region.area.451481', '凭祥市', NULL, '451481', 30, NULL, 1, 16, 2223, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2231, '2020-03-29 14:20:36.000', 1, 'system.region.province.460000', '海南省', 'city', '460000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2232, '2020-03-29 14:20:36.000', 1, 'system.region.city.460100', '海口市', 'area', '460100', 30, NULL, 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2233, '2020-03-29 14:20:36.000', 1, 'system.region.area.460105', '秀英区', NULL, '460105', 30, NULL, 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2234, '2020-03-29 14:20:36.000', 1, 'system.region.area.460106', '龙华区', NULL, '460106', 30, NULL, 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2235, '2020-03-29 14:20:36.000', 1, 'system.region.area.460107', '琼山区', NULL, '460107', 30, NULL, 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2236, '2020-03-29 14:20:36.000', 1, 'system.region.area.460108', '美兰区', NULL, '460108', 30, NULL, 1, 16, 2232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2237, '2020-03-29 14:20:36.000', 1, 'system.region.city.460200', '三亚市', 'area', '460200', 30, NULL, 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2238, '2020-03-29 14:20:36.000', 1, 'system.region.area.460202', '海棠区', NULL, '460202', 30, NULL, 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2239, '2020-03-29 14:20:36.000', 1, 'system.region.area.460203', '吉阳区', NULL, '460203', 30, NULL, 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2240, '2020-03-29 14:20:36.000', 1, 'system.region.area.460204', '天涯区', NULL, '460204', 30, NULL, 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2241, '2020-03-29 14:20:36.000', 1, 'system.region.area.460205', '崖州区', NULL, '460205', 30, NULL, 1, 16, 2237, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2242, '2020-03-29 14:20:36.000', 1, 'system.region.city.460300', '三沙市', 'area', '460300', 30, NULL, 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2243, '2020-03-29 14:20:36.000', 1, 'system.region.city.460400', '儋州市', 'area', '460400', 30, NULL, 1, 15, 2231, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2244, '2020-03-29 14:20:36.000', 1, 'system.region.area.469001', '五指山市', NULL, '469001', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2245, '2020-03-29 14:20:36.000', 1, 'system.region.area.469002', '琼海市', NULL, '469002', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2246, '2020-03-29 14:20:36.000', 1, 'system.region.area.469005', '文昌市', NULL, '469005', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2247, '2020-03-29 14:20:36.000', 1, 'system.region.area.469006', '万宁市', NULL, '469006', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2248, '2020-03-29 14:20:36.000', 1, 'system.region.area.469007', '东方市', NULL, '469007', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2249, '2020-03-29 14:20:36.000', 1, 'system.region.area.469021', '定安县', NULL, '469021', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2250, '2020-03-29 14:20:36.000', 1, 'system.region.area.469022', '屯昌县', NULL, '469022', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2251, '2020-03-29 14:20:36.000', 1, 'system.region.area.469023', '澄迈县', NULL, '469023', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2252, '2020-03-29 14:20:36.000', 1, 'system.region.area.469024', '临高县', NULL, '469024', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2253, '2020-03-29 14:20:36.000', 1, 'system.region.area.469025', '白沙黎族自治县', NULL, '469025', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2254, '2020-03-29 14:20:36.000', 1, 'system.region.area.469026', '昌江黎族自治县', NULL, '469026', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2255, '2020-03-29 14:20:36.000', 1, 'system.region.area.469027', '乐东黎族自治县', NULL, '469027', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2256, '2020-03-29 14:20:36.000', 1, 'system.region.area.469028', '陵水黎族自治县', NULL, '469028', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2257, '2020-03-29 14:20:36.000', 1, 'system.region.area.469029', '保亭黎族苗族自治县', NULL, '469029', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2258, '2020-03-29 14:20:36.000', 1, 'system.region.area.469030', '琼中黎族苗族自治县', NULL, '469030', 30, NULL, 1, 16, 2243, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2259, '2020-03-29 14:20:36.000', 1, 'system.region.province.500000', '重庆市', 'area', '500000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2260, '2020-03-29 14:20:36.000', 1, 'system.region.area.500101', '万州区', NULL, '500101', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2261, '2020-03-29 14:20:36.000', 1, 'system.region.area.500102', '涪陵区', NULL, '500102', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2262, '2020-03-29 14:20:36.000', 1, 'system.region.area.500103', '渝中区', NULL, '500103', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2263, '2020-03-29 14:20:36.000', 1, 'system.region.area.500104', '大渡口区', NULL, '500104', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2264, '2020-03-29 14:20:36.000', 1, 'system.region.area.500105', '江北区', NULL, '500105', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2265, '2020-03-29 14:20:36.000', 1, 'system.region.area.500106', '沙坪坝区', NULL, '500106', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2266, '2020-03-29 14:20:36.000', 1, 'system.region.area.500107', '九龙坡区', NULL, '500107', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2267, '2020-03-29 14:20:36.000', 1, 'system.region.area.500108', '南岸区', NULL, '500108', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2268, '2020-03-29 14:20:36.000', 1, 'system.region.area.500109', '北碚区', NULL, '500109', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2269, '2020-03-29 14:20:36.000', 1, 'system.region.area.500110', '綦江区', NULL, '500110', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2270, '2020-03-29 14:20:36.000', 1, 'system.region.area.500111', '大足区', NULL, '500111', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2271, '2020-03-29 14:20:36.000', 1, 'system.region.area.500112', '渝北区', NULL, '500112', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2272, '2020-03-29 14:20:36.000', 1, 'system.region.area.500113', '巴南区', NULL, '500113', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2273, '2020-03-29 14:20:36.000', 1, 'system.region.area.500114', '黔江区', NULL, '500114', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2274, '2020-03-29 14:20:36.000', 1, 'system.region.area.500115', '长寿区', NULL, '500115', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2275, '2020-03-29 14:20:36.000', 1, 'system.region.area.500116', '江津区', NULL, '500116', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2276, '2020-03-29 14:20:36.000', 1, 'system.region.area.500117', '合川区', NULL, '500117', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2277, '2020-03-29 14:20:36.000', 1, 'system.region.area.500118', '永川区', NULL, '500118', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2278, '2020-03-29 14:20:36.000', 1, 'system.region.area.500119', '南川区', NULL, '500119', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2279, '2020-03-29 14:20:36.000', 1, 'system.region.area.500120', '璧山区', NULL, '500120', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2280, '2020-03-29 14:20:36.000', 1, 'system.region.area.500151', '铜梁区', NULL, '500151', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2281, '2020-03-29 14:20:36.000', 1, 'system.region.area.500152', '潼南区', NULL, '500152', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2282, '2020-03-29 14:20:36.000', 1, 'system.region.area.500153', '荣昌区', NULL, '500153', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2283, '2020-03-29 14:20:36.000', 1, 'system.region.area.500154', '开州区', NULL, '500154', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2284, '2020-03-29 14:20:36.000', 1, 'system.region.area.500155', '梁平区', NULL, '500155', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2285, '2020-03-29 14:20:36.000', 1, 'system.region.area.500156', '武隆区', NULL, '500156', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2286, '2020-03-29 14:20:36.000', 1, 'system.region.area.500229', '城口县', NULL, '500229', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2287, '2020-03-29 14:20:36.000', 1, 'system.region.area.500230', '丰都县', NULL, '500230', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2288, '2020-03-29 14:20:36.000', 1, 'system.region.area.500231', '垫江县', NULL, '500231', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2289, '2020-03-29 14:20:36.000', 1, 'system.region.area.500233', '忠县', NULL, '500233', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2290, '2020-03-29 14:20:36.000', 1, 'system.region.area.500235', '云阳县', NULL, '500235', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2291, '2020-03-29 14:20:36.000', 1, 'system.region.area.500236', '奉节县', NULL, '500236', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2292, '2020-03-29 14:20:36.000', 1, 'system.region.area.500237', '巫山县', NULL, '500237', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2293, '2020-03-29 14:20:36.000', 1, 'system.region.area.500238', '巫溪县', NULL, '500238', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2294, '2020-03-29 14:20:36.000', 1, 'system.region.area.500240', '石柱土家族自治县', NULL, '500240', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2295, '2020-03-29 14:20:36.000', 1, 'system.region.area.500241', '秀山土家族苗族自治县', NULL, '500241', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2296, '2020-03-29 14:20:36.000', 1, 'system.region.area.500242', '酉阳土家族苗族自治县', NULL, '500242', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2297, '2020-03-29 14:20:36.000', 1, 'system.region.area.500243', '彭水苗族土家族自治县', NULL, '500243', 30, NULL, 1, 16, 2259, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2298, '2020-03-29 14:20:36.000', 1, 'system.region.province.510000', '四川省', 'city', '510000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2299, '2020-03-29 14:20:36.000', 1, 'system.region.city.510100', '成都市', 'area', '510100', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2300, '2020-03-29 14:20:36.000', 1, 'system.region.area.510104', '锦江区', NULL, '510104', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2301, '2020-03-29 14:20:36.000', 1, 'system.region.area.510105', '青羊区', NULL, '510105', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2302, '2020-03-29 14:20:36.000', 1, 'system.region.area.510106', '金牛区', NULL, '510106', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2303, '2020-03-29 14:20:36.000', 1, 'system.region.area.510107', '武侯区', NULL, '510107', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2304, '2020-03-29 14:20:36.000', 1, 'system.region.area.510108', '成华区', NULL, '510108', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2305, '2020-03-29 14:20:36.000', 1, 'system.region.area.510112', '龙泉驿区', NULL, '510112', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2306, '2020-03-29 14:20:36.000', 1, 'system.region.area.510113', '青白江区', NULL, '510113', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2307, '2020-03-29 14:20:36.000', 1, 'system.region.area.510114', '新都区', NULL, '510114', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2308, '2020-03-29 14:20:36.000', 1, 'system.region.area.510115', '温江区', NULL, '510115', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2309, '2020-03-29 14:20:36.000', 1, 'system.region.area.510116', '双流区', NULL, '510116', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2310, '2020-03-29 14:20:36.000', 1, 'system.region.area.510117', '郫都区', NULL, '510117', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2311, '2020-03-29 14:20:36.000', 1, 'system.region.area.510121', '金堂县', NULL, '510121', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2312, '2020-03-29 14:20:36.000', 1, 'system.region.area.510129', '大邑县', NULL, '510129', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2313, '2020-03-29 14:20:36.000', 1, 'system.region.area.510131', '蒲江县', NULL, '510131', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2314, '2020-03-29 14:20:36.000', 1, 'system.region.area.510132', '新津县', NULL, '510132', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2315, '2020-03-29 14:20:36.000', 1, 'system.region.area.510181', '都江堰市', NULL, '510181', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2316, '2020-03-29 14:20:36.000', 1, 'system.region.area.510182', '彭州市', NULL, '510182', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2317, '2020-03-29 14:20:36.000', 1, 'system.region.area.510183', '邛崃市', NULL, '510183', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2318, '2020-03-29 14:20:36.000', 1, 'system.region.area.510184', '崇州市', NULL, '510184', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2319, '2020-03-29 14:20:36.000', 1, 'system.region.area.510185', '简阳市', NULL, '510185', 30, NULL, 1, 16, 2299, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2320, '2020-03-29 14:20:36.000', 1, 'system.region.city.510300', '自贡市', 'area', '510300', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2321, '2020-03-29 14:20:36.000', 1, 'system.region.area.510302', '自流井区', NULL, '510302', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2322, '2020-03-29 14:20:36.000', 1, 'system.region.area.510303', '贡井区', NULL, '510303', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2323, '2020-03-29 14:20:36.000', 1, 'system.region.area.510304', '大安区', NULL, '510304', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2324, '2020-03-29 14:20:36.000', 1, 'system.region.area.510311', '沿滩区', NULL, '510311', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2325, '2020-03-29 14:20:36.000', 1, 'system.region.area.510321', '荣县', NULL, '510321', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2326, '2020-03-29 14:20:36.000', 1, 'system.region.area.510322', '富顺县', NULL, '510322', 30, NULL, 1, 16, 2320, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2327, '2020-03-29 14:20:36.000', 1, 'system.region.city.510400', '攀枝花市', 'area', '510400', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2328, '2020-03-29 14:20:36.000', 1, 'system.region.area.510402', '东区', NULL, '510402', 30, NULL, 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2329, '2020-03-29 14:20:36.000', 1, 'system.region.area.510403', '西区', NULL, '510403', 30, NULL, 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2330, '2020-03-29 14:20:36.000', 1, 'system.region.area.510411', '仁和区', NULL, '510411', 30, NULL, 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2331, '2020-03-29 14:20:36.000', 1, 'system.region.area.510421', '米易县', NULL, '510421', 30, NULL, 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2332, '2020-03-29 14:20:36.000', 1, 'system.region.area.510422', '盐边县', NULL, '510422', 30, NULL, 1, 16, 2327, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2333, '2020-03-29 14:20:36.000', 1, 'system.region.city.510500', '泸州市', 'area', '510500', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2334, '2020-03-29 14:20:36.000', 1, 'system.region.area.510502', '江阳区', NULL, '510502', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2335, '2020-03-29 14:20:36.000', 1, 'system.region.area.510503', '纳溪区', NULL, '510503', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2336, '2020-03-29 14:20:36.000', 1, 'system.region.area.510504', '龙马潭区', NULL, '510504', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2337, '2020-03-29 14:20:36.000', 1, 'system.region.area.510521', '泸县', NULL, '510521', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2338, '2020-03-29 14:20:36.000', 1, 'system.region.area.510522', '合江县', NULL, '510522', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2339, '2020-03-29 14:20:36.000', 1, 'system.region.area.510524', '叙永县', NULL, '510524', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2340, '2020-03-29 14:20:36.000', 1, 'system.region.area.510525', '古蔺县', NULL, '510525', 30, NULL, 1, 16, 2333, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2341, '2020-03-29 14:20:36.000', 1, 'system.region.city.510600', '德阳市', 'area', '510600', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2342, '2020-03-29 14:20:36.000', 1, 'system.region.area.510603', '旌阳区', NULL, '510603', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2343, '2020-03-29 14:20:36.000', 1, 'system.region.area.510604', '罗江区', NULL, '510604', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2344, '2020-03-29 14:20:36.000', 1, 'system.region.area.510623', '中江县', NULL, '510623', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2345, '2020-03-29 14:20:36.000', 1, 'system.region.area.510681', '广汉市', NULL, '510681', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2346, '2020-03-29 14:20:36.000', 1, 'system.region.area.510682', '什邡市', NULL, '510682', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2347, '2020-03-29 14:20:36.000', 1, 'system.region.area.510683', '绵竹市', NULL, '510683', 30, NULL, 1, 16, 2341, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2348, '2020-03-29 14:20:36.000', 1, 'system.region.city.510700', '绵阳市', 'area', '510700', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2349, '2020-03-29 14:20:36.000', 1, 'system.region.area.510703', '涪城区', NULL, '510703', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2350, '2020-03-29 14:20:36.000', 1, 'system.region.area.510704', '游仙区', NULL, '510704', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2351, '2020-03-29 14:20:36.000', 1, 'system.region.area.510705', '安州区', NULL, '510705', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2352, '2020-03-29 14:20:36.000', 1, 'system.region.area.510722', '三台县', NULL, '510722', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2353, '2020-03-29 14:20:36.000', 1, 'system.region.area.510723', '盐亭县', NULL, '510723', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2354, '2020-03-29 14:20:36.000', 1, 'system.region.area.510725', '梓潼县', NULL, '510725', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2355, '2020-03-29 14:20:36.000', 1, 'system.region.area.510726', '北川羌族自治县', NULL, '510726', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2356, '2020-03-29 14:20:36.000', 1, 'system.region.area.510727', '平武县', NULL, '510727', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2357, '2020-03-29 14:20:36.000', 1, 'system.region.area.510781', '江油市', NULL, '510781', 30, NULL, 1, 16, 2348, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2358, '2020-03-29 14:20:36.000', 1, 'system.region.city.510800', '广元市', 'area', '510800', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2359, '2020-03-29 14:20:36.000', 1, 'system.region.area.510802', '利州区', NULL, '510802', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2360, '2020-03-29 14:20:36.000', 1, 'system.region.area.510811', '昭化区', NULL, '510811', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2361, '2020-03-29 14:20:36.000', 1, 'system.region.area.510812', '朝天区', NULL, '510812', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2362, '2020-03-29 14:20:36.000', 1, 'system.region.area.510821', '旺苍县', NULL, '510821', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2363, '2020-03-29 14:20:36.000', 1, 'system.region.area.510822', '青川县', NULL, '510822', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2364, '2020-03-29 14:20:36.000', 1, 'system.region.area.510823', '剑阁县', NULL, '510823', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2365, '2020-03-29 14:20:36.000', 1, 'system.region.area.510824', '苍溪县', NULL, '510824', 30, NULL, 1, 16, 2358, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2366, '2020-03-29 14:20:36.000', 1, 'system.region.city.510900', '遂宁市', 'area', '510900', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2367, '2020-03-29 14:20:36.000', 1, 'system.region.area.510903', '船山区', NULL, '510903', 30, NULL, 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2368, '2020-03-29 14:20:36.000', 1, 'system.region.area.510904', '安居区', NULL, '510904', 30, NULL, 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2369, '2020-03-29 14:20:36.000', 1, 'system.region.area.510921', '蓬溪县', NULL, '510921', 30, NULL, 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2370, '2020-03-29 14:20:36.000', 1, 'system.region.area.510923', '大英县', NULL, '510923', 30, NULL, 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2371, '2020-03-29 14:20:36.000', 1, 'system.region.area.510981', '射洪市', NULL, '510981', 30, NULL, 1, 16, 2366, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2372, '2020-03-29 14:20:36.000', 1, 'system.region.city.511000', '内江市', 'area', '511000', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2373, '2020-03-29 14:20:36.000', 1, 'system.region.area.511002', '市中区', NULL, '511002', 30, NULL, 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2374, '2020-03-29 14:20:36.000', 1, 'system.region.area.511011', '东兴区', NULL, '511011', 30, NULL, 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2375, '2020-03-29 14:20:36.000', 1, 'system.region.area.511024', '威远县', NULL, '511024', 30, NULL, 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2376, '2020-03-29 14:20:36.000', 1, 'system.region.area.511025', '资中县', NULL, '511025', 30, NULL, 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2377, '2020-03-29 14:20:36.000', 1, 'system.region.area.511083', '隆昌市', NULL, '511083', 30, NULL, 1, 16, 2372, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2378, '2020-03-29 14:20:36.000', 1, 'system.region.city.511100', '乐山市', 'area', '511100', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2379, '2020-03-29 14:20:36.000', 1, 'system.region.area.511102', '市中区', NULL, '511102', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2380, '2020-03-29 14:20:36.000', 1, 'system.region.area.511111', '沙湾区', NULL, '511111', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2381, '2020-03-29 14:20:36.000', 1, 'system.region.area.511112', '五通桥区', NULL, '511112', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2382, '2020-03-29 14:20:36.000', 1, 'system.region.area.511113', '金口河区', NULL, '511113', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2383, '2020-03-29 14:20:36.000', 1, 'system.region.area.511123', '犍为县', NULL, '511123', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2384, '2020-03-29 14:20:36.000', 1, 'system.region.area.511124', '井研县', NULL, '511124', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2385, '2020-03-29 14:20:36.000', 1, 'system.region.area.511126', '夹江县', NULL, '511126', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2386, '2020-03-29 14:20:36.000', 1, 'system.region.area.511129', '沐川县', NULL, '511129', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2387, '2020-03-29 14:20:36.000', 1, 'system.region.area.511132', '峨边彝族自治县', NULL, '511132', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2388, '2020-03-29 14:20:36.000', 1, 'system.region.area.511133', '马边彝族自治县', NULL, '511133', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2389, '2020-03-29 14:20:36.000', 1, 'system.region.area.511181', '峨眉山市', NULL, '511181', 30, NULL, 1, 16, 2378, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2390, '2020-03-29 14:20:36.000', 1, 'system.region.city.511300', '南充市', 'area', '511300', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2391, '2020-03-29 14:20:36.000', 1, 'system.region.area.511302', '顺庆区', NULL, '511302', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2392, '2020-03-29 14:20:36.000', 1, 'system.region.area.511303', '高坪区', NULL, '511303', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2393, '2020-03-29 14:20:36.000', 1, 'system.region.area.511304', '嘉陵区', NULL, '511304', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2394, '2020-03-29 14:20:36.000', 1, 'system.region.area.511321', '南部县', NULL, '511321', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2395, '2020-03-29 14:20:36.000', 1, 'system.region.area.511322', '营山县', NULL, '511322', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2396, '2020-03-29 14:20:36.000', 1, 'system.region.area.511323', '蓬安县', NULL, '511323', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2397, '2020-03-29 14:20:36.000', 1, 'system.region.area.511324', '仪陇县', NULL, '511324', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2398, '2020-03-29 14:20:36.000', 1, 'system.region.area.511325', '西充县', NULL, '511325', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2399, '2020-03-29 14:20:36.000', 1, 'system.region.area.511381', '阆中市', NULL, '511381', 30, NULL, 1, 16, 2390, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2400, '2020-03-29 14:20:36.000', 1, 'system.region.city.511400', '眉山市', 'area', '511400', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2401, '2020-03-29 14:20:36.000', 1, 'system.region.area.511402', '东坡区', NULL, '511402', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2402, '2020-03-29 14:20:36.000', 1, 'system.region.area.511403', '彭山区', NULL, '511403', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2403, '2020-03-29 14:20:36.000', 1, 'system.region.area.511421', '仁寿县', NULL, '511421', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2404, '2020-03-29 14:20:36.000', 1, 'system.region.area.511423', '洪雅县', NULL, '511423', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2405, '2020-03-29 14:20:36.000', 1, 'system.region.area.511424', '丹棱县', NULL, '511424', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2406, '2020-03-29 14:20:36.000', 1, 'system.region.area.511425', '青神县', NULL, '511425', 30, NULL, 1, 16, 2400, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2407, '2020-03-29 14:20:36.000', 1, 'system.region.city.511500', '宜宾市', 'area', '511500', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2408, '2020-03-29 14:20:36.000', 1, 'system.region.area.511502', '翠屏区', NULL, '511502', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2409, '2020-03-29 14:20:36.000', 1, 'system.region.area.511503', '南溪区', NULL, '511503', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2410, '2020-03-29 14:20:36.000', 1, 'system.region.area.511504', '叙州区', NULL, '511504', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2411, '2020-03-29 14:20:36.000', 1, 'system.region.area.511523', '江安县', NULL, '511523', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2412, '2020-03-29 14:20:36.000', 1, 'system.region.area.511524', '长宁县', NULL, '511524', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2413, '2020-03-29 14:20:36.000', 1, 'system.region.area.511525', '高县', NULL, '511525', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2414, '2020-03-29 14:20:36.000', 1, 'system.region.area.511526', '珙县', NULL, '511526', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2415, '2020-03-29 14:20:36.000', 1, 'system.region.area.511527', '筠连县', NULL, '511527', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2416, '2020-03-29 14:20:36.000', 1, 'system.region.area.511528', '兴文县', NULL, '511528', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2417, '2020-03-29 14:20:36.000', 1, 'system.region.area.511529', '屏山县', NULL, '511529', 30, NULL, 1, 16, 2407, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2418, '2020-03-29 14:20:36.000', 1, 'system.region.city.511600', '广安市', 'area', '511600', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2419, '2020-03-29 14:20:36.000', 1, 'system.region.area.511602', '广安区', NULL, '511602', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2420, '2020-03-29 14:20:36.000', 1, 'system.region.area.511603', '前锋区', NULL, '511603', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2421, '2020-03-29 14:20:36.000', 1, 'system.region.area.511621', '岳池县', NULL, '511621', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2422, '2020-03-29 14:20:36.000', 1, 'system.region.area.511622', '武胜县', NULL, '511622', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2423, '2020-03-29 14:20:36.000', 1, 'system.region.area.511623', '邻水县', NULL, '511623', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2424, '2020-03-29 14:20:36.000', 1, 'system.region.area.511681', '华蓥市', NULL, '511681', 30, NULL, 1, 16, 2418, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2425, '2020-03-29 14:20:36.000', 1, 'system.region.city.511700', '达州市', 'area', '511700', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2426, '2020-03-29 14:20:36.000', 1, 'system.region.area.511702', '通川区', NULL, '511702', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2427, '2020-03-29 14:20:36.000', 1, 'system.region.area.511703', '达川区', NULL, '511703', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2428, '2020-03-29 14:20:36.000', 1, 'system.region.area.511722', '宣汉县', NULL, '511722', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2429, '2020-03-29 14:20:36.000', 1, 'system.region.area.511723', '开江县', NULL, '511723', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2430, '2020-03-29 14:20:36.000', 1, 'system.region.area.511724', '大竹县', NULL, '511724', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2431, '2020-03-29 14:20:36.000', 1, 'system.region.area.511725', '渠县', NULL, '511725', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2432, '2020-03-29 14:20:36.000', 1, 'system.region.area.511781', '万源市', NULL, '511781', 30, NULL, 1, 16, 2425, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2433, '2020-03-29 14:20:36.000', 1, 'system.region.city.511800', '雅安市', 'area', '511800', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2434, '2020-03-29 14:20:36.000', 1, 'system.region.area.511802', '雨城区', NULL, '511802', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2435, '2020-03-29 14:20:36.000', 1, 'system.region.area.511803', '名山区', NULL, '511803', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2436, '2020-03-29 14:20:36.000', 1, 'system.region.area.511822', '荥经县', NULL, '511822', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2437, '2020-03-29 14:20:36.000', 1, 'system.region.area.511823', '汉源县', NULL, '511823', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2438, '2020-03-29 14:20:36.000', 1, 'system.region.area.511824', '石棉县', NULL, '511824', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2439, '2020-03-29 14:20:36.000', 1, 'system.region.area.511825', '天全县', NULL, '511825', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2440, '2020-03-29 14:20:36.000', 1, 'system.region.area.511826', '芦山县', NULL, '511826', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2441, '2020-03-29 14:20:36.000', 1, 'system.region.area.511827', '宝兴县', NULL, '511827', 30, NULL, 1, 16, 2433, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2442, '2020-03-29 14:20:36.000', 1, 'system.region.city.511900', '巴中市', 'area', '511900', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2443, '2020-03-29 14:20:36.000', 1, 'system.region.area.511902', '巴州区', NULL, '511902', 30, NULL, 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2444, '2020-03-29 14:20:36.000', 1, 'system.region.area.511903', '恩阳区', NULL, '511903', 30, NULL, 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2445, '2020-03-29 14:20:36.000', 1, 'system.region.area.511921', '通江县', NULL, '511921', 30, NULL, 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2446, '2020-03-29 14:20:36.000', 1, 'system.region.area.511922', '南江县', NULL, '511922', 30, NULL, 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2447, '2020-03-29 14:20:36.000', 1, 'system.region.area.511923', '平昌县', NULL, '511923', 30, NULL, 1, 16, 2442, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2448, '2020-03-29 14:20:36.000', 1, 'system.region.city.512000', '资阳市', 'area', '512000', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2449, '2020-03-29 14:20:36.000', 1, 'system.region.area.512002', '雁江区', NULL, '512002', 30, NULL, 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2450, '2020-03-29 14:20:36.000', 1, 'system.region.area.512021', '安岳县', NULL, '512021', 30, NULL, 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2451, '2020-03-29 14:20:36.000', 1, 'system.region.area.512022', '乐至县', NULL, '512022', 30, NULL, 1, 16, 2448, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2452, '2020-03-29 14:20:36.000', 1, 'system.region.city.513200', '阿坝藏族羌族自治州', 'area', '513200', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2453, '2020-03-29 14:20:36.000', 1, 'system.region.area.513201', '马尔康市', NULL, '513201', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2454, '2020-03-29 14:20:36.000', 1, 'system.region.area.513221', '汶川县', NULL, '513221', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2455, '2020-03-29 14:20:36.000', 1, 'system.region.area.513222', '理县', NULL, '513222', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2456, '2020-03-29 14:20:36.000', 1, 'system.region.area.513223', '茂县', NULL, '513223', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2457, '2020-03-29 14:20:36.000', 1, 'system.region.area.513224', '松潘县', NULL, '513224', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2458, '2020-03-29 14:20:36.000', 1, 'system.region.area.513225', '九寨沟县', NULL, '513225', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2459, '2020-03-29 14:20:36.000', 1, 'system.region.area.513226', '金川县', NULL, '513226', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2460, '2020-03-29 14:20:36.000', 1, 'system.region.area.513227', '小金县', NULL, '513227', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2461, '2020-03-29 14:20:36.000', 1, 'system.region.area.513228', '黑水县', NULL, '513228', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2462, '2020-03-29 14:20:36.000', 1, 'system.region.area.513230', '壤塘县', NULL, '513230', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2463, '2020-03-29 14:20:36.000', 1, 'system.region.area.513231', '阿坝县', NULL, '513231', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2464, '2020-03-29 14:20:36.000', 1, 'system.region.area.513232', '若尔盖县', NULL, '513232', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2465, '2020-03-29 14:20:36.000', 1, 'system.region.area.513233', '红原县', NULL, '513233', 30, NULL, 1, 16, 2452, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2466, '2020-03-29 14:20:36.000', 1, 'system.region.city.513300', '甘孜藏族自治州', 'area', '513300', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2467, '2020-03-29 14:20:36.000', 1, 'system.region.area.513301', '康定市', NULL, '513301', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2468, '2020-03-29 14:20:36.000', 1, 'system.region.area.513322', '泸定县', NULL, '513322', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2469, '2020-03-29 14:20:36.000', 1, 'system.region.area.513323', '丹巴县', NULL, '513323', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2470, '2020-03-29 14:20:36.000', 1, 'system.region.area.513324', '九龙县', NULL, '513324', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2471, '2020-03-29 14:20:36.000', 1, 'system.region.area.513325', '雅江县', NULL, '513325', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2472, '2020-03-29 14:20:36.000', 1, 'system.region.area.513326', '道孚县', NULL, '513326', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2473, '2020-03-29 14:20:36.000', 1, 'system.region.area.513327', '炉霍县', NULL, '513327', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2474, '2020-03-29 14:20:36.000', 1, 'system.region.area.513328', '甘孜县', NULL, '513328', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2475, '2020-03-29 14:20:36.000', 1, 'system.region.area.513329', '新龙县', NULL, '513329', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2476, '2020-03-29 14:20:36.000', 1, 'system.region.area.513330', '德格县', NULL, '513330', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2477, '2020-03-29 14:20:36.000', 1, 'system.region.area.513331', '白玉县', NULL, '513331', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2478, '2020-03-29 14:20:36.000', 1, 'system.region.area.513332', '石渠县', NULL, '513332', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2479, '2020-03-29 14:20:36.000', 1, 'system.region.area.513333', '色达县', NULL, '513333', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2480, '2020-03-29 14:20:36.000', 1, 'system.region.area.513334', '理塘县', NULL, '513334', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2481, '2020-03-29 14:20:36.000', 1, 'system.region.area.513335', '巴塘县', NULL, '513335', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2482, '2020-03-29 14:20:36.000', 1, 'system.region.area.513336', '乡城县', NULL, '513336', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2483, '2020-03-29 14:20:36.000', 1, 'system.region.area.513337', '稻城县', NULL, '513337', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2484, '2020-03-29 14:20:36.000', 1, 'system.region.area.513338', '得荣县', NULL, '513338', 30, NULL, 1, 16, 2466, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2485, '2020-03-29 14:20:36.000', 1, 'system.region.city.513400', '凉山彝族自治州', 'area', '513400', 30, NULL, 1, 15, 2298, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2486, '2020-03-29 14:20:36.000', 1, 'system.region.area.513401', '西昌市', NULL, '513401', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2487, '2020-03-29 14:20:36.000', 1, 'system.region.area.513422', '木里藏族自治县', NULL, '513422', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2488, '2020-03-29 14:20:36.000', 1, 'system.region.area.513423', '盐源县', NULL, '513423', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2489, '2020-03-29 14:20:36.000', 1, 'system.region.area.513424', '德昌县', NULL, '513424', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2490, '2020-03-29 14:20:36.000', 1, 'system.region.area.513425', '会理县', NULL, '513425', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2491, '2020-03-29 14:20:36.000', 1, 'system.region.area.513426', '会东县', NULL, '513426', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2492, '2020-03-29 14:20:36.000', 1, 'system.region.area.513427', '宁南县', NULL, '513427', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2493, '2020-03-29 14:20:36.000', 1, 'system.region.area.513428', '普格县', NULL, '513428', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2494, '2020-03-29 14:20:36.000', 1, 'system.region.area.513429', '布拖县', NULL, '513429', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2495, '2020-03-29 14:20:36.000', 1, 'system.region.area.513430', '金阳县', NULL, '513430', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2496, '2020-03-29 14:20:36.000', 1, 'system.region.area.513431', '昭觉县', NULL, '513431', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2497, '2020-03-29 14:20:36.000', 1, 'system.region.area.513432', '喜德县', NULL, '513432', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2498, '2020-03-29 14:20:36.000', 1, 'system.region.area.513433', '冕宁县', NULL, '513433', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2499, '2020-03-29 14:20:36.000', 1, 'system.region.area.513434', '越西县', NULL, '513434', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2500, '2020-03-29 14:20:36.000', 1, 'system.region.area.513435', '甘洛县', NULL, '513435', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2501, '2020-03-29 14:20:36.000', 1, 'system.region.area.513436', '美姑县', NULL, '513436', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2502, '2020-03-29 14:20:36.000', 1, 'system.region.area.513437', '雷波县', NULL, '513437', 30, NULL, 1, 16, 2485, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2503, '2020-03-29 14:20:36.000', 1, 'system.region.province.520000', '贵州省', 'city', '520000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2504, '2020-03-29 14:20:36.000', 1, 'system.region.city.520100', '贵阳市', 'area', '520100', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2505, '2020-03-29 14:20:36.000', 1, 'system.region.area.520102', '南明区', NULL, '520102', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2506, '2020-03-29 14:20:36.000', 1, 'system.region.area.520103', '云岩区', NULL, '520103', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2507, '2020-03-29 14:20:36.000', 1, 'system.region.area.520111', '花溪区', NULL, '520111', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2508, '2020-03-29 14:20:36.000', 1, 'system.region.area.520112', '乌当区', NULL, '520112', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2509, '2020-03-29 14:20:36.000', 1, 'system.region.area.520113', '白云区', NULL, '520113', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2510, '2020-03-29 14:20:36.000', 1, 'system.region.area.520115', '观山湖区', NULL, '520115', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2511, '2020-03-29 14:20:36.000', 1, 'system.region.area.520121', '开阳县', NULL, '520121', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2512, '2020-03-29 14:20:36.000', 1, 'system.region.area.520122', '息烽县', NULL, '520122', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2513, '2020-03-29 14:20:36.000', 1, 'system.region.area.520123', '修文县', NULL, '520123', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2514, '2020-03-29 14:20:36.000', 1, 'system.region.area.520181', '清镇市', NULL, '520181', 30, NULL, 1, 16, 2504, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2515, '2020-03-29 14:20:36.000', 1, 'system.region.city.520200', '六盘水市', 'area', '520200', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2516, '2020-03-29 14:20:36.000', 1, 'system.region.area.520201', '钟山区', NULL, '520201', 30, NULL, 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2517, '2020-03-29 14:20:36.000', 1, 'system.region.area.520203', '六枝特区', NULL, '520203', 30, NULL, 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2518, '2020-03-29 14:20:36.000', 1, 'system.region.area.520221', '水城县', NULL, '520221', 30, NULL, 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2519, '2020-03-29 14:20:36.000', 1, 'system.region.area.520281', '盘州市', NULL, '520281', 30, NULL, 1, 16, 2515, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2520, '2020-03-29 14:20:36.000', 1, 'system.region.city.520300', '遵义市', 'area', '520300', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2521, '2020-03-29 14:20:36.000', 1, 'system.region.area.520302', '红花岗区', NULL, '520302', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2522, '2020-03-29 14:20:36.000', 1, 'system.region.area.520303', '汇川区', NULL, '520303', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2523, '2020-03-29 14:20:36.000', 1, 'system.region.area.520304', '播州区', NULL, '520304', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2524, '2020-03-29 14:20:36.000', 1, 'system.region.area.520322', '桐梓县', NULL, '520322', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2525, '2020-03-29 14:20:36.000', 1, 'system.region.area.520323', '绥阳县', NULL, '520323', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2526, '2020-03-29 14:20:36.000', 1, 'system.region.area.520324', '正安县', NULL, '520324', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2527, '2020-03-29 14:20:36.000', 1, 'system.region.area.520325', '道真仡佬族苗族自治县', NULL, '520325', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2528, '2020-03-29 14:20:36.000', 1, 'system.region.area.520326', '务川仡佬族苗族自治县', NULL, '520326', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2529, '2020-03-29 14:20:36.000', 1, 'system.region.area.520327', '凤冈县', NULL, '520327', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2530, '2020-03-29 14:20:36.000', 1, 'system.region.area.520328', '湄潭县', NULL, '520328', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2531, '2020-03-29 14:20:36.000', 1, 'system.region.area.520329', '余庆县', NULL, '520329', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2532, '2020-03-29 14:20:36.000', 1, 'system.region.area.520330', '习水县', NULL, '520330', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2533, '2020-03-29 14:20:36.000', 1, 'system.region.area.520381', '赤水市', NULL, '520381', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2534, '2020-03-29 14:20:36.000', 1, 'system.region.area.520382', '仁怀市', NULL, '520382', 30, NULL, 1, 16, 2520, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2535, '2020-03-29 14:20:36.000', 1, 'system.region.city.520400', '安顺市', 'area', '520400', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2536, '2020-03-29 14:20:36.000', 1, 'system.region.area.520402', '西秀区', NULL, '520402', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2537, '2020-03-29 14:20:36.000', 1, 'system.region.area.520403', '平坝区', NULL, '520403', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2538, '2020-03-29 14:20:36.000', 1, 'system.region.area.520422', '普定县', NULL, '520422', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2539, '2020-03-29 14:20:36.000', 1, 'system.region.area.520423', '镇宁布依族苗族自治县', NULL, '520423', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2540, '2020-03-29 14:20:36.000', 1, 'system.region.area.520424', '关岭布依族苗族自治县', NULL, '520424', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2541, '2020-03-29 14:20:36.000', 1, 'system.region.area.520425', '紫云苗族布依族自治县', NULL, '520425', 30, NULL, 1, 16, 2535, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2542, '2020-03-29 14:20:36.000', 1, 'system.region.city.520500', '毕节市', 'area', '520500', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2543, '2020-03-29 14:20:36.000', 1, 'system.region.area.520502', '七星关区', NULL, '520502', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2544, '2020-03-29 14:20:36.000', 1, 'system.region.area.520521', '大方县', NULL, '520521', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2545, '2020-03-29 14:20:36.000', 1, 'system.region.area.520522', '黔西县', NULL, '520522', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2546, '2020-03-29 14:20:36.000', 1, 'system.region.area.520523', '金沙县', NULL, '520523', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2547, '2020-03-29 14:20:36.000', 1, 'system.region.area.520524', '织金县', NULL, '520524', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2548, '2020-03-29 14:20:36.000', 1, 'system.region.area.520525', '纳雍县', NULL, '520525', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2549, '2020-03-29 14:20:36.000', 1, 'system.region.area.520526', '威宁彝族回族苗族自治县', NULL, '520526', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2550, '2020-03-29 14:20:36.000', 1, 'system.region.area.520527', '赫章县', NULL, '520527', 30, NULL, 1, 16, 2542, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2551, '2020-03-29 14:20:36.000', 1, 'system.region.city.520600', '铜仁市', 'area', '520600', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2552, '2020-03-29 14:20:36.000', 1, 'system.region.area.520602', '碧江区', NULL, '520602', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2553, '2020-03-29 14:20:36.000', 1, 'system.region.area.520603', '万山区', NULL, '520603', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2554, '2020-03-29 14:20:36.000', 1, 'system.region.area.520621', '江口县', NULL, '520621', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2555, '2020-03-29 14:20:36.000', 1, 'system.region.area.520622', '玉屏侗族自治县', NULL, '520622', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2556, '2020-03-29 14:20:36.000', 1, 'system.region.area.520623', '石阡县', NULL, '520623', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2557, '2020-03-29 14:20:36.000', 1, 'system.region.area.520624', '思南县', NULL, '520624', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2558, '2020-03-29 14:20:36.000', 1, 'system.region.area.520625', '印江土家族苗族自治县', NULL, '520625', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2559, '2020-03-29 14:20:36.000', 1, 'system.region.area.520626', '德江县', NULL, '520626', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2560, '2020-03-29 14:20:36.000', 1, 'system.region.area.520627', '沿河土家族自治县', NULL, '520627', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2561, '2020-03-29 14:20:36.000', 1, 'system.region.area.520628', '松桃苗族自治县', NULL, '520628', 30, NULL, 1, 16, 2551, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2562, '2020-03-29 14:20:36.000', 1, 'system.region.city.522300', '黔西南布依族苗族自治州', 'area', '522300', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2563, '2020-03-29 14:20:36.000', 1, 'system.region.area.522301', '兴义市', NULL, '522301', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2564, '2020-03-29 14:20:36.000', 1, 'system.region.area.522302', '兴仁市', NULL, '522302', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2565, '2020-03-29 14:20:36.000', 1, 'system.region.area.522323', '普安县', NULL, '522323', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2566, '2020-03-29 14:20:36.000', 1, 'system.region.area.522324', '晴隆县', NULL, '522324', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2567, '2020-03-29 14:20:36.000', 1, 'system.region.area.522325', '贞丰县', NULL, '522325', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2568, '2020-03-29 14:20:36.000', 1, 'system.region.area.522326', '望谟县', NULL, '522326', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2569, '2020-03-29 14:20:36.000', 1, 'system.region.area.522327', '册亨县', NULL, '522327', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2570, '2020-03-29 14:20:36.000', 1, 'system.region.area.522328', '安龙县', NULL, '522328', 30, NULL, 1, 16, 2562, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2571, '2020-03-29 14:20:36.000', 1, 'system.region.city.522600', '黔东南苗族侗族自治州', 'area', '522600', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2572, '2020-03-29 14:20:36.000', 1, 'system.region.area.522601', '凯里市', NULL, '522601', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2573, '2020-03-29 14:20:36.000', 1, 'system.region.area.522622', '黄平县', NULL, '522622', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2574, '2020-03-29 14:20:36.000', 1, 'system.region.area.522623', '施秉县', NULL, '522623', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2575, '2020-03-29 14:20:36.000', 1, 'system.region.area.522624', '三穗县', NULL, '522624', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2576, '2020-03-29 14:20:36.000', 1, 'system.region.area.522625', '镇远县', NULL, '522625', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2577, '2020-03-29 14:20:36.000', 1, 'system.region.area.522626', '岑巩县', NULL, '522626', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2578, '2020-03-29 14:20:36.000', 1, 'system.region.area.522627', '天柱县', NULL, '522627', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2579, '2020-03-29 14:20:36.000', 1, 'system.region.area.522628', '锦屏县', NULL, '522628', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2580, '2020-03-29 14:20:36.000', 1, 'system.region.area.522629', '剑河县', NULL, '522629', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2581, '2020-03-29 14:20:36.000', 1, 'system.region.area.522630', '台江县', NULL, '522630', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2582, '2020-03-29 14:20:36.000', 1, 'system.region.area.522631', '黎平县', NULL, '522631', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2583, '2020-03-29 14:20:36.000', 1, 'system.region.area.522632', '榕江县', NULL, '522632', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2584, '2020-03-29 14:20:36.000', 1, 'system.region.area.522633', '从江县', NULL, '522633', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2585, '2020-03-29 14:20:36.000', 1, 'system.region.area.522634', '雷山县', NULL, '522634', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2586, '2020-03-29 14:20:36.000', 1, 'system.region.area.522635', '麻江县', NULL, '522635', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2587, '2020-03-29 14:20:36.000', 1, 'system.region.area.522636', '丹寨县', NULL, '522636', 30, NULL, 1, 16, 2571, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2588, '2020-03-29 14:20:36.000', 1, 'system.region.city.522700', '黔南布依族苗族自治州', 'area', '522700', 30, NULL, 1, 15, 2503, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2589, '2020-03-29 14:20:36.000', 1, 'system.region.area.522701', '都匀市', NULL, '522701', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2590, '2020-03-29 14:20:36.000', 1, 'system.region.area.522702', '福泉市', NULL, '522702', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2591, '2020-03-29 14:20:36.000', 1, 'system.region.area.522722', '荔波县', NULL, '522722', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2592, '2020-03-29 14:20:36.000', 1, 'system.region.area.522723', '贵定县', NULL, '522723', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2593, '2020-03-29 14:20:36.000', 1, 'system.region.area.522725', '瓮安县', NULL, '522725', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2594, '2020-03-29 14:20:36.000', 1, 'system.region.area.522726', '独山县', NULL, '522726', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2595, '2020-03-29 14:20:36.000', 1, 'system.region.area.522727', '平塘县', NULL, '522727', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2596, '2020-03-29 14:20:36.000', 1, 'system.region.area.522728', '罗甸县', NULL, '522728', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2597, '2020-03-29 14:20:36.000', 1, 'system.region.area.522729', '长顺县', NULL, '522729', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2598, '2020-03-29 14:20:36.000', 1, 'system.region.area.522730', '龙里县', NULL, '522730', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2599, '2020-03-29 14:20:36.000', 1, 'system.region.area.522731', '惠水县', NULL, '522731', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2600, '2020-03-29 14:20:36.000', 1, 'system.region.area.522732', '三都水族自治县', NULL, '522732', 30, NULL, 1, 16, 2588, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2601, '2020-03-29 14:20:36.000', 1, 'system.region.province.530000', '云南省', 'city', '530000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2602, '2020-03-29 14:20:36.000', 1, 'system.region.city.530100', '昆明市', 'area', '530100', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2603, '2020-03-29 14:20:36.000', 1, 'system.region.area.530102', '五华区', NULL, '530102', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2604, '2020-03-29 14:20:36.000', 1, 'system.region.area.530103', '盘龙区', NULL, '530103', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2605, '2020-03-29 14:20:36.000', 1, 'system.region.area.530111', '官渡区', NULL, '530111', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2606, '2020-03-29 14:20:36.000', 1, 'system.region.area.530112', '西山区', NULL, '530112', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2607, '2020-03-29 14:20:36.000', 1, 'system.region.area.530113', '东川区', NULL, '530113', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2608, '2020-03-29 14:20:36.000', 1, 'system.region.area.530114', '呈贡区', NULL, '530114', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2609, '2020-03-29 14:20:36.000', 1, 'system.region.area.530115', '晋宁区', NULL, '530115', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2610, '2020-03-29 14:20:36.000', 1, 'system.region.area.530124', '富民县', NULL, '530124', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2611, '2020-03-29 14:20:36.000', 1, 'system.region.area.530125', '宜良县', NULL, '530125', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2612, '2020-03-29 14:20:36.000', 1, 'system.region.area.530126', '石林彝族自治县', NULL, '530126', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2613, '2020-03-29 14:20:36.000', 1, 'system.region.area.530127', '嵩明县', NULL, '530127', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2614, '2020-03-29 14:20:36.000', 1, 'system.region.area.530128', '禄劝彝族苗族自治县', NULL, '530128', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2615, '2020-03-29 14:20:36.000', 1, 'system.region.area.530129', '寻甸回族彝族自治县', NULL, '530129', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2616, '2020-03-29 14:20:36.000', 1, 'system.region.area.530181', '安宁市', NULL, '530181', 30, NULL, 1, 16, 2602, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2617, '2020-03-29 14:20:36.000', 1, 'system.region.city.530300', '曲靖市', 'area', '530300', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2618, '2020-03-29 14:20:36.000', 1, 'system.region.area.530302', '麒麟区', NULL, '530302', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2619, '2020-03-29 14:20:36.000', 1, 'system.region.area.530303', '沾益区', NULL, '530303', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2620, '2020-03-29 14:20:36.000', 1, 'system.region.area.530304', '马龙区', NULL, '530304', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2621, '2020-03-29 14:20:36.000', 1, 'system.region.area.530322', '陆良县', NULL, '530322', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2622, '2020-03-29 14:20:36.000', 1, 'system.region.area.530323', '师宗县', NULL, '530323', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2623, '2020-03-29 14:20:36.000', 1, 'system.region.area.530324', '罗平县', NULL, '530324', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2624, '2020-03-29 14:20:36.000', 1, 'system.region.area.530325', '富源县', NULL, '530325', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2625, '2020-03-29 14:20:36.000', 1, 'system.region.area.530326', '会泽县', NULL, '530326', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2626, '2020-03-29 14:20:36.000', 1, 'system.region.area.530381', '宣威市', NULL, '530381', 30, NULL, 1, 16, 2617, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2627, '2020-03-29 14:20:36.000', 1, 'system.region.city.530400', '玉溪市', 'area', '530400', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2628, '2020-03-29 14:20:36.000', 1, 'system.region.area.530402', '红塔区', NULL, '530402', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2629, '2020-03-29 14:20:36.000', 1, 'system.region.area.530403', '江川区', NULL, '530403', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2630, '2020-03-29 14:20:36.000', 1, 'system.region.area.530423', '通海县', NULL, '530423', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2631, '2020-03-29 14:20:36.000', 1, 'system.region.area.530424', '华宁县', NULL, '530424', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2632, '2020-03-29 14:20:36.000', 1, 'system.region.area.530425', '易门县', NULL, '530425', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2633, '2020-03-29 14:20:36.000', 1, 'system.region.area.530426', '峨山彝族自治县', NULL, '530426', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2634, '2020-03-29 14:20:36.000', 1, 'system.region.area.530427', '新平彝族傣族自治县', NULL, '530427', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2635, '2020-03-29 14:20:36.000', 1, 'system.region.area.530428', '元江哈尼族彝族傣族自治县', NULL, '530428', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2636, '2020-03-29 14:20:36.000', 1, 'system.region.area.530481', '澄江市', NULL, '530481', 30, NULL, 1, 16, 2627, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2637, '2020-03-29 14:20:36.000', 1, 'system.region.city.530500', '保山市', 'area', '530500', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2638, '2020-03-29 14:20:36.000', 1, 'system.region.area.530502', '隆阳区', NULL, '530502', 30, NULL, 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2639, '2020-03-29 14:20:36.000', 1, 'system.region.area.530521', '施甸县', NULL, '530521', 30, NULL, 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2640, '2020-03-29 14:20:36.000', 1, 'system.region.area.530523', '龙陵县', NULL, '530523', 30, NULL, 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2641, '2020-03-29 14:20:36.000', 1, 'system.region.area.530524', '昌宁县', NULL, '530524', 30, NULL, 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2642, '2020-03-29 14:20:36.000', 1, 'system.region.area.530581', '腾冲市', NULL, '530581', 30, NULL, 1, 16, 2637, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2643, '2020-03-29 14:20:36.000', 1, 'system.region.city.530600', '昭通市', 'area', '530600', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2644, '2020-03-29 14:20:36.000', 1, 'system.region.area.530602', '昭阳区', NULL, '530602', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2645, '2020-03-29 14:20:36.000', 1, 'system.region.area.530621', '鲁甸县', NULL, '530621', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2646, '2020-03-29 14:20:36.000', 1, 'system.region.area.530622', '巧家县', NULL, '530622', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2647, '2020-03-29 14:20:36.000', 1, 'system.region.area.530623', '盐津县', NULL, '530623', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2648, '2020-03-29 14:20:36.000', 1, 'system.region.area.530624', '大关县', NULL, '530624', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2649, '2020-03-29 14:20:36.000', 1, 'system.region.area.530625', '永善县', NULL, '530625', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2650, '2020-03-29 14:20:36.000', 1, 'system.region.area.530626', '绥江县', NULL, '530626', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2651, '2020-03-29 14:20:36.000', 1, 'system.region.area.530627', '镇雄县', NULL, '530627', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2652, '2020-03-29 14:20:36.000', 1, 'system.region.area.530628', '彝良县', NULL, '530628', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2653, '2020-03-29 14:20:36.000', 1, 'system.region.area.530629', '威信县', NULL, '530629', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2654, '2020-03-29 14:20:36.000', 1, 'system.region.area.530681', '水富市', NULL, '530681', 30, NULL, 1, 16, 2643, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2655, '2020-03-29 14:20:36.000', 1, 'system.region.city.530700', '丽江市', 'area', '530700', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2656, '2020-03-29 14:20:36.000', 1, 'system.region.area.530702', '古城区', NULL, '530702', 30, NULL, 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2657, '2020-03-29 14:20:36.000', 1, 'system.region.area.530721', '玉龙纳西族自治县', NULL, '530721', 30, NULL, 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2658, '2020-03-29 14:20:36.000', 1, 'system.region.area.530722', '永胜县', NULL, '530722', 30, NULL, 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2659, '2020-03-29 14:20:36.000', 1, 'system.region.area.530723', '华坪县', NULL, '530723', 30, NULL, 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2660, '2020-03-29 14:20:36.000', 1, 'system.region.area.530724', '宁蒗彝族自治县', NULL, '530724', 30, NULL, 1, 16, 2655, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2661, '2020-03-29 14:20:36.000', 1, 'system.region.city.530800', '普洱市', 'area', '530800', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2662, '2020-03-29 14:20:36.000', 1, 'system.region.area.530802', '思茅区', NULL, '530802', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2663, '2020-03-29 14:20:36.000', 1, 'system.region.area.530821', '宁洱哈尼族彝族自治县', NULL, '530821', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2664, '2020-03-29 14:20:36.000', 1, 'system.region.area.530822', '墨江哈尼族自治县', NULL, '530822', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2665, '2020-03-29 14:20:36.000', 1, 'system.region.area.530823', '景东彝族自治县', NULL, '530823', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2666, '2020-03-29 14:20:36.000', 1, 'system.region.area.530824', '景谷傣族彝族自治县', NULL, '530824', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2667, '2020-03-29 14:20:36.000', 1, 'system.region.area.530825', '镇沅彝族哈尼族拉祜族自治县', NULL, '530825', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2668, '2020-03-29 14:20:36.000', 1, 'system.region.area.530826', '江城哈尼族彝族自治县', NULL, '530826', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2669, '2020-03-29 14:20:36.000', 1, 'system.region.area.530827', '孟连傣族拉祜族佤族自治县', NULL, '530827', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2670, '2020-03-29 14:20:36.000', 1, 'system.region.area.530828', '澜沧拉祜族自治县', NULL, '530828', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2671, '2020-03-29 14:20:36.000', 1, 'system.region.area.530829', '西盟佤族自治县', NULL, '530829', 30, NULL, 1, 16, 2661, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2672, '2020-03-29 14:20:36.000', 1, 'system.region.city.530900', '临沧市', 'area', '530900', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2673, '2020-03-29 14:20:36.000', 1, 'system.region.area.530902', '临翔区', NULL, '530902', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2674, '2020-03-29 14:20:36.000', 1, 'system.region.area.530921', '凤庆县', NULL, '530921', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2675, '2020-03-29 14:20:36.000', 1, 'system.region.area.530922', '云县', NULL, '530922', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2676, '2020-03-29 14:20:36.000', 1, 'system.region.area.530923', '永德县', NULL, '530923', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2677, '2020-03-29 14:20:36.000', 1, 'system.region.area.530924', '镇康县', NULL, '530924', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2678, '2020-03-29 14:20:36.000', 1, 'system.region.area.530925', '双江拉祜族佤族布朗族傣族自治县', NULL, '530925', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2679, '2020-03-29 14:20:36.000', 1, 'system.region.area.530926', '耿马傣族佤族自治县', NULL, '530926', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2680, '2020-03-29 14:20:36.000', 1, 'system.region.area.530927', '沧源佤族自治县', NULL, '530927', 30, NULL, 1, 16, 2672, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2681, '2020-03-29 14:20:36.000', 1, 'system.region.city.532300', '楚雄彝族自治州', 'area', '532300', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2682, '2020-03-29 14:20:36.000', 1, 'system.region.area.532301', '楚雄市', NULL, '532301', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2683, '2020-03-29 14:20:36.000', 1, 'system.region.area.532322', '双柏县', NULL, '532322', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2684, '2020-03-29 14:20:36.000', 1, 'system.region.area.532323', '牟定县', NULL, '532323', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2685, '2020-03-29 14:20:36.000', 1, 'system.region.area.532324', '南华县', NULL, '532324', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2686, '2020-03-29 14:20:36.000', 1, 'system.region.area.532325', '姚安县', NULL, '532325', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2687, '2020-03-29 14:20:36.000', 1, 'system.region.area.532326', '大姚县', NULL, '532326', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2688, '2020-03-29 14:20:36.000', 1, 'system.region.area.532327', '永仁县', NULL, '532327', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2689, '2020-03-29 14:20:36.000', 1, 'system.region.area.532328', '元谋县', NULL, '532328', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2690, '2020-03-29 14:20:36.000', 1, 'system.region.area.532329', '武定县', NULL, '532329', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2691, '2020-03-29 14:20:36.000', 1, 'system.region.area.532331', '禄丰县', NULL, '532331', 30, NULL, 1, 16, 2681, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2692, '2020-03-29 14:20:36.000', 1, 'system.region.city.532500', '红河哈尼族彝族自治州', 'area', '532500', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2693, '2020-03-29 14:20:36.000', 1, 'system.region.area.532501', '个旧市', NULL, '532501', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2694, '2020-03-29 14:20:36.000', 1, 'system.region.area.532502', '开远市', NULL, '532502', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2695, '2020-03-29 14:20:36.000', 1, 'system.region.area.532503', '蒙自市', NULL, '532503', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2696, '2020-03-29 14:20:36.000', 1, 'system.region.area.532504', '弥勒市', NULL, '532504', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2697, '2020-03-29 14:20:36.000', 1, 'system.region.area.532523', '屏边苗族自治县', NULL, '532523', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2698, '2020-03-29 14:20:36.000', 1, 'system.region.area.532524', '建水县', NULL, '532524', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2699, '2020-03-29 14:20:36.000', 1, 'system.region.area.532525', '石屏县', NULL, '532525', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2700, '2020-03-29 14:20:36.000', 1, 'system.region.area.532527', '泸西县', NULL, '532527', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2701, '2020-03-29 14:20:36.000', 1, 'system.region.area.532528', '元阳县', NULL, '532528', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2702, '2020-03-29 14:20:36.000', 1, 'system.region.area.532529', '红河县', NULL, '532529', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2703, '2020-03-29 14:20:36.000', 1, 'system.region.area.532530', '金平苗族瑶族傣族自治县', NULL, '532530', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2704, '2020-03-29 14:20:36.000', 1, 'system.region.area.532531', '绿春县', NULL, '532531', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2705, '2020-03-29 14:20:36.000', 1, 'system.region.area.532532', '河口瑶族自治县', NULL, '532532', 30, NULL, 1, 16, 2692, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2706, '2020-03-29 14:20:36.000', 1, 'system.region.city.532600', '文山壮族苗族自治州', 'area', '532600', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2707, '2020-03-29 14:20:36.000', 1, 'system.region.area.532601', '文山市', NULL, '532601', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2708, '2020-03-29 14:20:36.000', 1, 'system.region.area.532622', '砚山县', NULL, '532622', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2709, '2020-03-29 14:20:36.000', 1, 'system.region.area.532623', '西畴县', NULL, '532623', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2710, '2020-03-29 14:20:36.000', 1, 'system.region.area.532624', '麻栗坡县', NULL, '532624', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2711, '2020-03-29 14:20:36.000', 1, 'system.region.area.532625', '马关县', NULL, '532625', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2712, '2020-03-29 14:20:36.000', 1, 'system.region.area.532626', '丘北县', NULL, '532626', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2713, '2020-03-29 14:20:36.000', 1, 'system.region.area.532627', '广南县', NULL, '532627', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2714, '2020-03-29 14:20:36.000', 1, 'system.region.area.532628', '富宁县', NULL, '532628', 30, NULL, 1, 16, 2706, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2715, '2020-03-29 14:20:36.000', 1, 'system.region.city.532800', '西双版纳傣族自治州', 'area', '532800', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2716, '2020-03-29 14:20:36.000', 1, 'system.region.area.532801', '景洪市', NULL, '532801', 30, NULL, 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2717, '2020-03-29 14:20:36.000', 1, 'system.region.area.532822', '勐海县', NULL, '532822', 30, NULL, 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2718, '2020-03-29 14:20:36.000', 1, 'system.region.area.532823', '勐腊县', NULL, '532823', 30, NULL, 1, 16, 2715, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2719, '2020-03-29 14:20:36.000', 1, 'system.region.city.532900', '大理白族自治州', 'area', '532900', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2720, '2020-03-29 14:20:36.000', 1, 'system.region.area.532901', '大理市', NULL, '532901', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2721, '2020-03-29 14:20:36.000', 1, 'system.region.area.532922', '漾濞彝族自治县', NULL, '532922', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2722, '2020-03-29 14:20:36.000', 1, 'system.region.area.532923', '祥云县', NULL, '532923', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2723, '2020-03-29 14:20:36.000', 1, 'system.region.area.532924', '宾川县', NULL, '532924', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2724, '2020-03-29 14:20:36.000', 1, 'system.region.area.532925', '弥渡县', NULL, '532925', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2725, '2020-03-29 14:20:36.000', 1, 'system.region.area.532926', '南涧彝族自治县', NULL, '532926', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2726, '2020-03-29 14:20:36.000', 1, 'system.region.area.532927', '巍山彝族回族自治县', NULL, '532927', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2727, '2020-03-29 14:20:36.000', 1, 'system.region.area.532928', '永平县', NULL, '532928', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2728, '2020-03-29 14:20:36.000', 1, 'system.region.area.532929', '云龙县', NULL, '532929', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2729, '2020-03-29 14:20:36.000', 1, 'system.region.area.532930', '洱源县', NULL, '532930', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2730, '2020-03-29 14:20:36.000', 1, 'system.region.area.532931', '剑川县', NULL, '532931', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2731, '2020-03-29 14:20:36.000', 1, 'system.region.area.532932', '鹤庆县', NULL, '532932', 30, NULL, 1, 16, 2719, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2732, '2020-03-29 14:20:36.000', 1, 'system.region.city.533100', '德宏傣族景颇族自治州', 'area', '533100', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2733, '2020-03-29 14:20:36.000', 1, 'system.region.area.533102', '瑞丽市', NULL, '533102', 30, NULL, 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2734, '2020-03-29 14:20:36.000', 1, 'system.region.area.533103', '芒市', NULL, '533103', 30, NULL, 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2735, '2020-03-29 14:20:36.000', 1, 'system.region.area.533122', '梁河县', NULL, '533122', 30, NULL, 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2736, '2020-03-29 14:20:36.000', 1, 'system.region.area.533123', '盈江县', NULL, '533123', 30, NULL, 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2737, '2020-03-29 14:20:36.000', 1, 'system.region.area.533124', '陇川县', NULL, '533124', 30, NULL, 1, 16, 2732, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2738, '2020-03-29 14:20:36.000', 1, 'system.region.city.533300', '怒江傈僳族自治州', 'area', '533300', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2739, '2020-03-29 14:20:36.000', 1, 'system.region.area.533301', '泸水市', NULL, '533301', 30, NULL, 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2740, '2020-03-29 14:20:36.000', 1, 'system.region.area.533323', '福贡县', NULL, '533323', 30, NULL, 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2741, '2020-03-29 14:20:36.000', 1, 'system.region.area.533324', '贡山独龙族怒族自治县', NULL, '533324', 30, NULL, 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2742, '2020-03-29 14:20:36.000', 1, 'system.region.area.533325', '兰坪白族普米族自治县', NULL, '533325', 30, NULL, 1, 16, 2738, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2743, '2020-03-29 14:20:36.000', 1, 'system.region.city.533400', '迪庆藏族自治州', 'area', '533400', 30, NULL, 1, 15, 2601, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2744, '2020-03-29 14:20:36.000', 1, 'system.region.area.533401', '香格里拉市', NULL, '533401', 30, NULL, 1, 16, 2743, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2745, '2020-03-29 14:20:36.000', 1, 'system.region.area.533422', '德钦县', NULL, '533422', 30, NULL, 1, 16, 2743, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2746, '2020-03-29 14:20:36.000', 1, 'system.region.area.533423', '维西傈僳族自治县', NULL, '533423', 30, NULL, 1, 16, 2743, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2747, '2020-03-29 14:20:36.000', 1, 'system.region.province.540000', '西藏自治区', 'city', '540000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2748, '2020-03-29 14:20:36.000', 1, 'system.region.city.540100', '拉萨市', 'area', '540100', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2749, '2020-03-29 14:20:36.000', 1, 'system.region.area.540102', '城关区', NULL, '540102', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2750, '2020-03-29 14:20:36.000', 1, 'system.region.area.540103', '堆龙德庆区', NULL, '540103', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2751, '2020-03-29 14:20:36.000', 1, 'system.region.area.540104', '达孜区', NULL, '540104', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2752, '2020-03-29 14:20:36.000', 1, 'system.region.area.540121', '林周县', NULL, '540121', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2753, '2020-03-29 14:20:36.000', 1, 'system.region.area.540122', '当雄县', NULL, '540122', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2754, '2020-03-29 14:20:36.000', 1, 'system.region.area.540123', '尼木县', NULL, '540123', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2755, '2020-03-29 14:20:36.000', 1, 'system.region.area.540124', '曲水县', NULL, '540124', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2756, '2020-03-29 14:20:36.000', 1, 'system.region.area.540127', '墨竹工卡县', NULL, '540127', 30, NULL, 1, 16, 2748, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2757, '2020-03-29 14:20:36.000', 1, 'system.region.city.540200', '日喀则市', 'area', '540200', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2758, '2020-03-29 14:20:36.000', 1, 'system.region.area.540202', '桑珠孜区', NULL, '540202', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2759, '2020-03-29 14:20:36.000', 1, 'system.region.area.540221', '南木林县', NULL, '540221', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2760, '2020-03-29 14:20:36.000', 1, 'system.region.area.540222', '江孜县', NULL, '540222', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2761, '2020-03-29 14:20:36.000', 1, 'system.region.area.540223', '定日县', NULL, '540223', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2762, '2020-03-29 14:20:36.000', 1, 'system.region.area.540224', '萨迦县', NULL, '540224', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2763, '2020-03-29 14:20:36.000', 1, 'system.region.area.540225', '拉孜县', NULL, '540225', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2764, '2020-03-29 14:20:36.000', 1, 'system.region.area.540226', '昂仁县', NULL, '540226', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2765, '2020-03-29 14:20:36.000', 1, 'system.region.area.540227', '谢通门县', NULL, '540227', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2766, '2020-03-29 14:20:36.000', 1, 'system.region.area.540228', '白朗县', NULL, '540228', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2767, '2020-03-29 14:20:36.000', 1, 'system.region.area.540229', '仁布县', NULL, '540229', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2768, '2020-03-29 14:20:36.000', 1, 'system.region.area.540230', '康马县', NULL, '540230', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2769, '2020-03-29 14:20:36.000', 1, 'system.region.area.540231', '定结县', NULL, '540231', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2770, '2020-03-29 14:20:36.000', 1, 'system.region.area.540232', '仲巴县', NULL, '540232', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2771, '2020-03-29 14:20:36.000', 1, 'system.region.area.540233', '亚东县', NULL, '540233', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2772, '2020-03-29 14:20:36.000', 1, 'system.region.area.540234', '吉隆县', NULL, '540234', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2773, '2020-03-29 14:20:36.000', 1, 'system.region.area.540235', '聂拉木县', NULL, '540235', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2774, '2020-03-29 14:20:36.000', 1, 'system.region.area.540236', '萨嘎县', NULL, '540236', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2775, '2020-03-29 14:20:36.000', 1, 'system.region.area.540237', '岗巴县', NULL, '540237', 30, NULL, 1, 16, 2757, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2776, '2020-03-29 14:20:36.000', 1, 'system.region.city.540300', '昌都市', 'area', '540300', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2777, '2020-03-29 14:20:36.000', 1, 'system.region.area.540302', '卡若区', NULL, '540302', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2778, '2020-03-29 14:20:36.000', 1, 'system.region.area.540321', '江达县', NULL, '540321', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2779, '2020-03-29 14:20:36.000', 1, 'system.region.area.540322', '贡觉县', NULL, '540322', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2780, '2020-03-29 14:20:36.000', 1, 'system.region.area.540323', '类乌齐县', NULL, '540323', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2781, '2020-03-29 14:20:36.000', 1, 'system.region.area.540324', '丁青县', NULL, '540324', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2782, '2020-03-29 14:20:36.000', 1, 'system.region.area.540325', '察雅县', NULL, '540325', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2783, '2020-03-29 14:20:36.000', 1, 'system.region.area.540326', '八宿县', NULL, '540326', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2784, '2020-03-29 14:20:36.000', 1, 'system.region.area.540327', '左贡县', NULL, '540327', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2785, '2020-03-29 14:20:36.000', 1, 'system.region.area.540328', '芒康县', NULL, '540328', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2786, '2020-03-29 14:20:36.000', 1, 'system.region.area.540329', '洛隆县', NULL, '540329', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2787, '2020-03-29 14:20:36.000', 1, 'system.region.area.540330', '边坝县', NULL, '540330', 30, NULL, 1, 16, 2776, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2788, '2020-03-29 14:20:36.000', 1, 'system.region.city.540400', '林芝市', 'area', '540400', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2789, '2020-03-29 14:20:36.000', 1, 'system.region.area.540402', '巴宜区', NULL, '540402', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2790, '2020-03-29 14:20:36.000', 1, 'system.region.area.540421', '工布江达县', NULL, '540421', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2791, '2020-03-29 14:20:36.000', 1, 'system.region.area.540422', '米林县', NULL, '540422', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2792, '2020-03-29 14:20:36.000', 1, 'system.region.area.540423', '墨脱县', NULL, '540423', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2793, '2020-03-29 14:20:36.000', 1, 'system.region.area.540424', '波密县', NULL, '540424', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2794, '2020-03-29 14:20:36.000', 1, 'system.region.area.540425', '察隅县', NULL, '540425', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2795, '2020-03-29 14:20:36.000', 1, 'system.region.area.540426', '朗县', NULL, '540426', 30, NULL, 1, 16, 2788, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2796, '2020-03-29 14:20:36.000', 1, 'system.region.city.540500', '山南市', 'area', '540500', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2797, '2020-03-29 14:20:36.000', 1, 'system.region.area.540502', '乃东区', NULL, '540502', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2798, '2020-03-29 14:20:36.000', 1, 'system.region.area.540521', '扎囊县', NULL, '540521', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2799, '2020-03-29 14:20:36.000', 1, 'system.region.area.540522', '贡嘎县', NULL, '540522', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2800, '2020-03-29 14:20:36.000', 1, 'system.region.area.540523', '桑日县', NULL, '540523', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2801, '2020-03-29 14:20:36.000', 1, 'system.region.area.540524', '琼结县', NULL, '540524', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2802, '2020-03-29 14:20:36.000', 1, 'system.region.area.540525', '曲松县', NULL, '540525', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2803, '2020-03-29 14:20:36.000', 1, 'system.region.area.540526', '措美县', NULL, '540526', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2804, '2020-03-29 14:20:36.000', 1, 'system.region.area.540527', '洛扎县', NULL, '540527', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2805, '2020-03-29 14:20:36.000', 1, 'system.region.area.540528', '加查县', NULL, '540528', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2806, '2020-03-29 14:20:36.000', 1, 'system.region.area.540529', '隆子县', NULL, '540529', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2807, '2020-03-29 14:20:36.000', 1, 'system.region.area.540530', '错那县', NULL, '540530', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2808, '2020-03-29 14:20:36.000', 1, 'system.region.area.540531', '浪卡子县', NULL, '540531', 30, NULL, 1, 16, 2796, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2809, '2020-03-29 14:20:36.000', 1, 'system.region.city.540600', '那曲市', 'area', '540600', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2810, '2020-03-29 14:20:36.000', 1, 'system.region.area.540602', '色尼区', NULL, '540602', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2811, '2020-03-29 14:20:36.000', 1, 'system.region.area.540621', '嘉黎县', NULL, '540621', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2812, '2020-03-29 14:20:36.000', 1, 'system.region.area.540622', '比如县', NULL, '540622', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2813, '2020-03-29 14:20:36.000', 1, 'system.region.area.540623', '聂荣县', NULL, '540623', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2814, '2020-03-29 14:20:36.000', 1, 'system.region.area.540624', '安多县', NULL, '540624', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2815, '2020-03-29 14:20:36.000', 1, 'system.region.area.540625', '申扎县', NULL, '540625', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2816, '2020-03-29 14:20:36.000', 1, 'system.region.area.540626', '索县', NULL, '540626', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2817, '2020-03-29 14:20:36.000', 1, 'system.region.area.540627', '班戈县', NULL, '540627', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2818, '2020-03-29 14:20:36.000', 1, 'system.region.area.540628', '巴青县', NULL, '540628', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2819, '2020-03-29 14:20:36.000', 1, 'system.region.area.540629', '尼玛县', NULL, '540629', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2820, '2020-03-29 14:20:36.000', 1, 'system.region.area.540630', '双湖县', NULL, '540630', 30, NULL, 1, 16, 2809, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2821, '2020-03-29 14:20:36.000', 1, 'system.region.city.542500', '阿里地区', 'area', '542500', 30, NULL, 1, 15, 2747, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2822, '2020-03-29 14:20:36.000', 1, 'system.region.area.542521', '普兰县', NULL, '542521', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2823, '2020-03-29 14:20:36.000', 1, 'system.region.area.542522', '札达县', NULL, '542522', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2824, '2020-03-29 14:20:36.000', 1, 'system.region.area.542523', '噶尔县', NULL, '542523', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2825, '2020-03-29 14:20:36.000', 1, 'system.region.area.542524', '日土县', NULL, '542524', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2826, '2020-03-29 14:20:36.000', 1, 'system.region.area.542525', '革吉县', NULL, '542525', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2827, '2020-03-29 14:20:36.000', 1, 'system.region.area.542526', '改则县', NULL, '542526', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2828, '2020-03-29 14:20:36.000', 1, 'system.region.area.542527', '措勤县', NULL, '542527', 30, NULL, 1, 16, 2821, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2829, '2020-03-29 14:20:36.000', 1, 'system.region.province.610000', '陕西省', 'city', '610000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2830, '2020-03-29 14:20:36.000', 1, 'system.region.city.610100', '西安市', 'area', '610100', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2831, '2020-03-29 14:20:36.000', 1, 'system.region.area.610102', '新城区', NULL, '610102', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2832, '2020-03-29 14:20:36.000', 1, 'system.region.area.610103', '碑林区', NULL, '610103', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2833, '2020-03-29 14:20:36.000', 1, 'system.region.area.610104', '莲湖区', NULL, '610104', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2834, '2020-03-29 14:20:36.000', 1, 'system.region.area.610111', '灞桥区', NULL, '610111', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2835, '2020-03-29 14:20:36.000', 1, 'system.region.area.610112', '未央区', NULL, '610112', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2836, '2020-03-29 14:20:36.000', 1, 'system.region.area.610113', '雁塔区', NULL, '610113', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2837, '2020-03-29 14:20:36.000', 1, 'system.region.area.610114', '阎良区', NULL, '610114', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2838, '2020-03-29 14:20:36.000', 1, 'system.region.area.610115', '临潼区', NULL, '610115', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2839, '2020-03-29 14:20:36.000', 1, 'system.region.area.610116', '长安区', NULL, '610116', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2840, '2020-03-29 14:20:36.000', 1, 'system.region.area.610117', '高陵区', NULL, '610117', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2841, '2020-03-29 14:20:36.000', 1, 'system.region.area.610118', '鄠邑区', NULL, '610118', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2842, '2020-03-29 14:20:36.000', 1, 'system.region.area.610122', '蓝田县', NULL, '610122', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2843, '2020-03-29 14:20:36.000', 1, 'system.region.area.610124', '周至县', NULL, '610124', 30, NULL, 1, 16, 2830, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2844, '2020-03-29 14:20:36.000', 1, 'system.region.city.610200', '铜川市', 'area', '610200', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2845, '2020-03-29 14:20:36.000', 1, 'system.region.area.610202', '王益区', NULL, '610202', 30, NULL, 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2846, '2020-03-29 14:20:36.000', 1, 'system.region.area.610203', '印台区', NULL, '610203', 30, NULL, 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2847, '2020-03-29 14:20:36.000', 1, 'system.region.area.610204', '耀州区', NULL, '610204', 30, NULL, 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2848, '2020-03-29 14:20:36.000', 1, 'system.region.area.610222', '宜君县', NULL, '610222', 30, NULL, 1, 16, 2844, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2849, '2020-03-29 14:20:36.000', 1, 'system.region.city.610300', '宝鸡市', 'area', '610300', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2850, '2020-03-29 14:20:36.000', 1, 'system.region.area.610302', '渭滨区', NULL, '610302', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2851, '2020-03-29 14:20:36.000', 1, 'system.region.area.610303', '金台区', NULL, '610303', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2852, '2020-03-29 14:20:36.000', 1, 'system.region.area.610304', '陈仓区', NULL, '610304', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2853, '2020-03-29 14:20:36.000', 1, 'system.region.area.610322', '凤翔县', NULL, '610322', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2854, '2020-03-29 14:20:36.000', 1, 'system.region.area.610323', '岐山县', NULL, '610323', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2855, '2020-03-29 14:20:36.000', 1, 'system.region.area.610324', '扶风县', NULL, '610324', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2856, '2020-03-29 14:20:36.000', 1, 'system.region.area.610326', '眉县', NULL, '610326', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2857, '2020-03-29 14:20:36.000', 1, 'system.region.area.610327', '陇县', NULL, '610327', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2858, '2020-03-29 14:20:36.000', 1, 'system.region.area.610328', '千阳县', NULL, '610328', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2859, '2020-03-29 14:20:36.000', 1, 'system.region.area.610329', '麟游县', NULL, '610329', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2860, '2020-03-29 14:20:36.000', 1, 'system.region.area.610330', '凤县', NULL, '610330', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2861, '2020-03-29 14:20:36.000', 1, 'system.region.area.610331', '太白县', NULL, '610331', 30, NULL, 1, 16, 2849, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2862, '2020-03-29 14:20:36.000', 1, 'system.region.city.610400', '咸阳市', 'area', '610400', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2863, '2020-03-29 14:20:36.000', 1, 'system.region.area.610402', '秦都区', NULL, '610402', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2864, '2020-03-29 14:20:36.000', 1, 'system.region.area.610403', '杨陵区', NULL, '610403', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2865, '2020-03-29 14:20:36.000', 1, 'system.region.area.610404', '渭城区', NULL, '610404', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2866, '2020-03-29 14:20:36.000', 1, 'system.region.area.610422', '三原县', NULL, '610422', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2867, '2020-03-29 14:20:36.000', 1, 'system.region.area.610423', '泾阳县', NULL, '610423', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2868, '2020-03-29 14:20:36.000', 1, 'system.region.area.610424', '乾县', NULL, '610424', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2869, '2020-03-29 14:20:36.000', 1, 'system.region.area.610425', '礼泉县', NULL, '610425', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2870, '2020-03-29 14:20:36.000', 1, 'system.region.area.610426', '永寿县', NULL, '610426', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2871, '2020-03-29 14:20:36.000', 1, 'system.region.area.610428', '长武县', NULL, '610428', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2872, '2020-03-29 14:20:36.000', 1, 'system.region.area.610429', '旬邑县', NULL, '610429', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2873, '2020-03-29 14:20:36.000', 1, 'system.region.area.610430', '淳化县', NULL, '610430', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2874, '2020-03-29 14:20:36.000', 1, 'system.region.area.610431', '武功县', NULL, '610431', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2875, '2020-03-29 14:20:36.000', 1, 'system.region.area.610481', '兴平市', NULL, '610481', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2876, '2020-03-29 14:20:36.000', 1, 'system.region.area.610482', '彬州市', NULL, '610482', 30, NULL, 1, 16, 2862, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2877, '2020-03-29 14:20:36.000', 1, 'system.region.city.610500', '渭南市', 'area', '610500', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2878, '2020-03-29 14:20:36.000', 1, 'system.region.area.610502', '临渭区', NULL, '610502', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2879, '2020-03-29 14:20:36.000', 1, 'system.region.area.610503', '华州区', NULL, '610503', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2880, '2020-03-29 14:20:36.000', 1, 'system.region.area.610522', '潼关县', NULL, '610522', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2881, '2020-03-29 14:20:36.000', 1, 'system.region.area.610523', '大荔县', NULL, '610523', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2882, '2020-03-29 14:20:36.000', 1, 'system.region.area.610524', '合阳县', NULL, '610524', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2883, '2020-03-29 14:20:36.000', 1, 'system.region.area.610525', '澄城县', NULL, '610525', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2884, '2020-03-29 14:20:36.000', 1, 'system.region.area.610526', '蒲城县', NULL, '610526', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2885, '2020-03-29 14:20:36.000', 1, 'system.region.area.610527', '白水县', NULL, '610527', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2886, '2020-03-29 14:20:36.000', 1, 'system.region.area.610528', '富平县', NULL, '610528', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2887, '2020-03-29 14:20:36.000', 1, 'system.region.area.610581', '韩城市', NULL, '610581', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2888, '2020-03-29 14:20:36.000', 1, 'system.region.area.610582', '华阴市', NULL, '610582', 30, NULL, 1, 16, 2877, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2889, '2020-03-29 14:20:36.000', 1, 'system.region.city.610600', '延安市', 'area', '610600', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2890, '2020-03-29 14:20:36.000', 1, 'system.region.area.610602', '宝塔区', NULL, '610602', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2891, '2020-03-29 14:20:36.000', 1, 'system.region.area.610603', '安塞区', NULL, '610603', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2892, '2020-03-29 14:20:36.000', 1, 'system.region.area.610621', '延长县', NULL, '610621', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2893, '2020-03-29 14:20:36.000', 1, 'system.region.area.610622', '延川县', NULL, '610622', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2894, '2020-03-29 14:20:36.000', 1, 'system.region.area.610625', '志丹县', NULL, '610625', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2895, '2020-03-29 14:20:36.000', 1, 'system.region.area.610626', '吴起县', NULL, '610626', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2896, '2020-03-29 14:20:36.000', 1, 'system.region.area.610627', '甘泉县', NULL, '610627', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2897, '2020-03-29 14:20:36.000', 1, 'system.region.area.610628', '富县', NULL, '610628', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2898, '2020-03-29 14:20:36.000', 1, 'system.region.area.610629', '洛川县', NULL, '610629', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2899, '2020-03-29 14:20:36.000', 1, 'system.region.area.610630', '宜川县', NULL, '610630', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2900, '2020-03-29 14:20:36.000', 1, 'system.region.area.610631', '黄龙县', NULL, '610631', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2901, '2020-03-29 14:20:36.000', 1, 'system.region.area.610632', '黄陵县', NULL, '610632', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2902, '2020-03-29 14:20:36.000', 1, 'system.region.area.610681', '子长市', NULL, '610681', 30, NULL, 1, 16, 2889, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2903, '2020-03-29 14:20:36.000', 1, 'system.region.city.610700', '汉中市', 'area', '610700', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2904, '2020-03-29 14:20:36.000', 1, 'system.region.area.610702', '汉台区', NULL, '610702', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2905, '2020-03-29 14:20:36.000', 1, 'system.region.area.610703', '南郑区', NULL, '610703', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2906, '2020-03-29 14:20:36.000', 1, 'system.region.area.610722', '城固县', NULL, '610722', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2907, '2020-03-29 14:20:36.000', 1, 'system.region.area.610723', '洋县', NULL, '610723', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2908, '2020-03-29 14:20:36.000', 1, 'system.region.area.610724', '西乡县', NULL, '610724', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2909, '2020-03-29 14:20:36.000', 1, 'system.region.area.610725', '勉县', NULL, '610725', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2910, '2020-03-29 14:20:36.000', 1, 'system.region.area.610726', '宁强县', NULL, '610726', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2911, '2020-03-29 14:20:36.000', 1, 'system.region.area.610727', '略阳县', NULL, '610727', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2912, '2020-03-29 14:20:36.000', 1, 'system.region.area.610728', '镇巴县', NULL, '610728', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2913, '2020-03-29 14:20:36.000', 1, 'system.region.area.610729', '留坝县', NULL, '610729', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2914, '2020-03-29 14:20:36.000', 1, 'system.region.area.610730', '佛坪县', NULL, '610730', 30, NULL, 1, 16, 2903, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2915, '2020-03-29 14:20:36.000', 1, 'system.region.city.610800', '榆林市', 'area', '610800', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2916, '2020-03-29 14:20:36.000', 1, 'system.region.area.610802', '榆阳区', NULL, '610802', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2917, '2020-03-29 14:20:36.000', 1, 'system.region.area.610803', '横山区', NULL, '610803', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2918, '2020-03-29 14:20:36.000', 1, 'system.region.area.610822', '府谷县', NULL, '610822', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2919, '2020-03-29 14:20:36.000', 1, 'system.region.area.610824', '靖边县', NULL, '610824', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2920, '2020-03-29 14:20:36.000', 1, 'system.region.area.610825', '定边县', NULL, '610825', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2921, '2020-03-29 14:20:36.000', 1, 'system.region.area.610826', '绥德县', NULL, '610826', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2922, '2020-03-29 14:20:36.000', 1, 'system.region.area.610827', '米脂县', NULL, '610827', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2923, '2020-03-29 14:20:36.000', 1, 'system.region.area.610828', '佳县', NULL, '610828', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2924, '2020-03-29 14:20:36.000', 1, 'system.region.area.610829', '吴堡县', NULL, '610829', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2925, '2020-03-29 14:20:36.000', 1, 'system.region.area.610830', '清涧县', NULL, '610830', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2926, '2020-03-29 14:20:36.000', 1, 'system.region.area.610831', '子洲县', NULL, '610831', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2927, '2020-03-29 14:20:36.000', 1, 'system.region.area.610881', '神木市', NULL, '610881', 30, NULL, 1, 16, 2915, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2928, '2020-03-29 14:20:36.000', 1, 'system.region.city.610900', '安康市', 'area', '610900', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2929, '2020-03-29 14:20:36.000', 1, 'system.region.area.610902', '汉滨区', NULL, '610902', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2930, '2020-03-29 14:20:36.000', 1, 'system.region.area.610921', '汉阴县', NULL, '610921', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2931, '2020-03-29 14:20:36.000', 1, 'system.region.area.610922', '石泉县', NULL, '610922', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2932, '2020-03-29 14:20:36.000', 1, 'system.region.area.610923', '宁陕县', NULL, '610923', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2933, '2020-03-29 14:20:36.000', 1, 'system.region.area.610924', '紫阳县', NULL, '610924', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2934, '2020-03-29 14:20:36.000', 1, 'system.region.area.610925', '岚皋县', NULL, '610925', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2935, '2020-03-29 14:20:36.000', 1, 'system.region.area.610926', '平利县', NULL, '610926', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2936, '2020-03-29 14:20:36.000', 1, 'system.region.area.610927', '镇坪县', NULL, '610927', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2937, '2020-03-29 14:20:36.000', 1, 'system.region.area.610928', '旬阳县', NULL, '610928', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2938, '2020-03-29 14:20:36.000', 1, 'system.region.area.610929', '白河县', NULL, '610929', 30, NULL, 1, 16, 2928, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2939, '2020-03-29 14:20:36.000', 1, 'system.region.city.611000', '商洛市', 'area', '611000', 30, NULL, 1, 15, 2829, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2940, '2020-03-29 14:20:36.000', 1, 'system.region.area.611002', '商州区', NULL, '611002', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2941, '2020-03-29 14:20:36.000', 1, 'system.region.area.611021', '洛南县', NULL, '611021', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2942, '2020-03-29 14:20:36.000', 1, 'system.region.area.611022', '丹凤县', NULL, '611022', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2943, '2020-03-29 14:20:36.000', 1, 'system.region.area.611023', '商南县', NULL, '611023', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2944, '2020-03-29 14:20:36.000', 1, 'system.region.area.611024', '山阳县', NULL, '611024', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2945, '2020-03-29 14:20:36.000', 1, 'system.region.area.611025', '镇安县', NULL, '611025', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2946, '2020-03-29 14:20:36.000', 1, 'system.region.area.611026', '柞水县', NULL, '611026', 30, NULL, 1, 16, 2939, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2947, '2020-03-29 14:20:36.000', 1, 'system.region.province.620000', '甘肃省', 'city', '620000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2948, '2020-03-29 14:20:36.000', 1, 'system.region.city.620100', '兰州市', 'area', '620100', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2949, '2020-03-29 14:20:36.000', 1, 'system.region.area.620102', '城关区', NULL, '620102', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2950, '2020-03-29 14:20:36.000', 1, 'system.region.area.620103', '七里河区', NULL, '620103', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2951, '2020-03-29 14:20:36.000', 1, 'system.region.area.620104', '西固区', NULL, '620104', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2952, '2020-03-29 14:20:36.000', 1, 'system.region.area.620105', '安宁区', NULL, '620105', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2953, '2020-03-29 14:20:36.000', 1, 'system.region.area.620111', '红古区', NULL, '620111', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2954, '2020-03-29 14:20:36.000', 1, 'system.region.area.620121', '永登县', NULL, '620121', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2955, '2020-03-29 14:20:36.000', 1, 'system.region.area.620122', '皋兰县', NULL, '620122', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2956, '2020-03-29 14:20:36.000', 1, 'system.region.area.620123', '榆中县', NULL, '620123', 30, NULL, 1, 16, 2948, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2957, '2020-03-29 14:20:36.000', 1, 'system.region.city.620200', '嘉峪关市', 'area', '620200', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2958, '2020-03-29 14:20:36.000', 1, 'system.region.city.620300', '金昌市', 'area', '620300', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2959, '2020-03-29 14:20:36.000', 1, 'system.region.area.620302', '金川区', NULL, '620302', 30, NULL, 1, 16, 2958, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2960, '2020-03-29 14:20:36.000', 1, 'system.region.area.620321', '永昌县', NULL, '620321', 30, NULL, 1, 16, 2958, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2961, '2020-03-29 14:20:36.000', 1, 'system.region.city.620400', '白银市', 'area', '620400', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2962, '2020-03-29 14:20:36.000', 1, 'system.region.area.620402', '白银区', NULL, '620402', 30, NULL, 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2963, '2020-03-29 14:20:36.000', 1, 'system.region.area.620403', '平川区', NULL, '620403', 30, NULL, 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2964, '2020-03-29 14:20:36.000', 1, 'system.region.area.620421', '靖远县', NULL, '620421', 30, NULL, 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2965, '2020-03-29 14:20:36.000', 1, 'system.region.area.620422', '会宁县', NULL, '620422', 30, NULL, 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2966, '2020-03-29 14:20:36.000', 1, 'system.region.area.620423', '景泰县', NULL, '620423', 30, NULL, 1, 16, 2961, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2967, '2020-03-29 14:20:36.000', 1, 'system.region.city.620500', '天水市', 'area', '620500', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2968, '2020-03-29 14:20:36.000', 1, 'system.region.area.620502', '秦州区', NULL, '620502', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2969, '2020-03-29 14:20:36.000', 1, 'system.region.area.620503', '麦积区', NULL, '620503', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2970, '2020-03-29 14:20:36.000', 1, 'system.region.area.620521', '清水县', NULL, '620521', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2971, '2020-03-29 14:20:36.000', 1, 'system.region.area.620522', '秦安县', NULL, '620522', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2972, '2020-03-29 14:20:36.000', 1, 'system.region.area.620523', '甘谷县', NULL, '620523', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2973, '2020-03-29 14:20:36.000', 1, 'system.region.area.620524', '武山县', NULL, '620524', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2974, '2020-03-29 14:20:36.000', 1, 'system.region.area.620525', '张家川回族自治县', NULL, '620525', 30, NULL, 1, 16, 2967, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2975, '2020-03-29 14:20:36.000', 1, 'system.region.city.620600', '武威市', 'area', '620600', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2976, '2020-03-29 14:20:36.000', 1, 'system.region.area.620602', '凉州区', NULL, '620602', 30, NULL, 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2977, '2020-03-29 14:20:36.000', 1, 'system.region.area.620621', '民勤县', NULL, '620621', 30, NULL, 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2978, '2020-03-29 14:20:36.000', 1, 'system.region.area.620622', '古浪县', NULL, '620622', 30, NULL, 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2979, '2020-03-29 14:20:36.000', 1, 'system.region.area.620623', '天祝藏族自治县', NULL, '620623', 30, NULL, 1, 16, 2975, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2980, '2020-03-29 14:20:36.000', 1, 'system.region.city.620700', '张掖市', 'area', '620700', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2981, '2020-03-29 14:20:36.000', 1, 'system.region.area.620702', '甘州区', NULL, '620702', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2982, '2020-03-29 14:20:36.000', 1, 'system.region.area.620721', '肃南裕固族自治县', NULL, '620721', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2983, '2020-03-29 14:20:36.000', 1, 'system.region.area.620722', '民乐县', NULL, '620722', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2984, '2020-03-29 14:20:36.000', 1, 'system.region.area.620723', '临泽县', NULL, '620723', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2985, '2020-03-29 14:20:36.000', 1, 'system.region.area.620724', '高台县', NULL, '620724', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2986, '2020-03-29 14:20:36.000', 1, 'system.region.area.620725', '山丹县', NULL, '620725', 30, NULL, 1, 16, 2980, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2987, '2020-03-29 14:20:36.000', 1, 'system.region.city.620800', '平凉市', 'area', '620800', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2988, '2020-03-29 14:20:36.000', 1, 'system.region.area.620802', '崆峒区', NULL, '620802', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2989, '2020-03-29 14:20:36.000', 1, 'system.region.area.620821', '泾川县', NULL, '620821', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2990, '2020-03-29 14:20:36.000', 1, 'system.region.area.620822', '灵台县', NULL, '620822', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2991, '2020-03-29 14:20:36.000', 1, 'system.region.area.620823', '崇信县', NULL, '620823', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2992, '2020-03-29 14:20:36.000', 1, 'system.region.area.620825', '庄浪县', NULL, '620825', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2993, '2020-03-29 14:20:36.000', 1, 'system.region.area.620826', '静宁县', NULL, '620826', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2994, '2020-03-29 14:20:36.000', 1, 'system.region.area.620881', '华亭市', NULL, '620881', 30, NULL, 1, 16, 2987, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2995, '2020-03-29 14:20:36.000', 1, 'system.region.city.620900', '酒泉市', 'area', '620900', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2996, '2020-03-29 14:20:36.000', 1, 'system.region.area.620902', '肃州区', NULL, '620902', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2997, '2020-03-29 14:20:36.000', 1, 'system.region.area.620921', '金塔县', NULL, '620921', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2998, '2020-03-29 14:20:36.000', 1, 'system.region.area.620922', '瓜州县', NULL, '620922', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (2999, '2020-03-29 14:20:36.000', 1, 'system.region.area.620923', '肃北蒙古族自治县', NULL, '620923', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3000, '2020-03-29 14:20:36.000', 1, 'system.region.area.620924', '阿克塞哈萨克族自治县', NULL, '620924', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3001, '2020-03-29 14:20:36.000', 1, 'system.region.area.620981', '玉门市', NULL, '620981', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3002, '2020-03-29 14:20:36.000', 1, 'system.region.area.620982', '敦煌市', NULL, '620982', 30, NULL, 1, 16, 2995, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3003, '2020-03-29 14:20:36.000', 1, 'system.region.city.621000', '庆阳市', 'area', '621000', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3004, '2020-03-29 14:20:36.000', 1, 'system.region.area.621002', '西峰区', NULL, '621002', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3005, '2020-03-29 14:20:36.000', 1, 'system.region.area.621021', '庆城县', NULL, '621021', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3006, '2020-03-29 14:20:36.000', 1, 'system.region.area.621022', '环县', NULL, '621022', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3007, '2020-03-29 14:20:36.000', 1, 'system.region.area.621023', '华池县', NULL, '621023', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3008, '2020-03-29 14:20:36.000', 1, 'system.region.area.621024', '合水县', NULL, '621024', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3009, '2020-03-29 14:20:36.000', 1, 'system.region.area.621025', '正宁县', NULL, '621025', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3010, '2020-03-29 14:20:36.000', 1, 'system.region.area.621026', '宁县', NULL, '621026', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3011, '2020-03-29 14:20:36.000', 1, 'system.region.area.621027', '镇原县', NULL, '621027', 30, NULL, 1, 16, 3003, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3012, '2020-03-29 14:20:36.000', 1, 'system.region.city.621100', '定西市', 'area', '621100', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3013, '2020-03-29 14:20:36.000', 1, 'system.region.area.621102', '安定区', NULL, '621102', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3014, '2020-03-29 14:20:36.000', 1, 'system.region.area.621121', '通渭县', NULL, '621121', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3015, '2020-03-29 14:20:36.000', 1, 'system.region.area.621122', '陇西县', NULL, '621122', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3016, '2020-03-29 14:20:36.000', 1, 'system.region.area.621123', '渭源县', NULL, '621123', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3017, '2020-03-29 14:20:36.000', 1, 'system.region.area.621124', '临洮县', NULL, '621124', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3018, '2020-03-29 14:20:36.000', 1, 'system.region.area.621125', '漳县', NULL, '621125', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3019, '2020-03-29 14:20:36.000', 1, 'system.region.area.621126', '岷县', NULL, '621126', 30, NULL, 1, 16, 3012, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3020, '2020-03-29 14:20:36.000', 1, 'system.region.city.621200', '陇南市', 'area', '621200', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3021, '2020-03-29 14:20:36.000', 1, 'system.region.area.621202', '武都区', NULL, '621202', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3022, '2020-03-29 14:20:36.000', 1, 'system.region.area.621221', '成县', NULL, '621221', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3023, '2020-03-29 14:20:36.000', 1, 'system.region.area.621222', '文县', NULL, '621222', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3024, '2020-03-29 14:20:36.000', 1, 'system.region.area.621223', '宕昌县', NULL, '621223', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3025, '2020-03-29 14:20:36.000', 1, 'system.region.area.621224', '康县', NULL, '621224', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3026, '2020-03-29 14:20:36.000', 1, 'system.region.area.621225', '西和县', NULL, '621225', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3027, '2020-03-29 14:20:36.000', 1, 'system.region.area.621226', '礼县', NULL, '621226', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3028, '2020-03-29 14:20:36.000', 1, 'system.region.area.621227', '徽县', NULL, '621227', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3029, '2020-03-29 14:20:36.000', 1, 'system.region.area.621228', '两当县', NULL, '621228', 30, NULL, 1, 16, 3020, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3030, '2020-03-29 14:20:36.000', 1, 'system.region.city.622900', '临夏回族自治州', 'area', '622900', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3031, '2020-03-29 14:20:36.000', 1, 'system.region.area.622901', '临夏市', NULL, '622901', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3032, '2020-03-29 14:20:36.000', 1, 'system.region.area.622921', '临夏县', NULL, '622921', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3033, '2020-03-29 14:20:36.000', 1, 'system.region.area.622922', '康乐县', NULL, '622922', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3034, '2020-03-29 14:20:36.000', 1, 'system.region.area.622923', '永靖县', NULL, '622923', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3035, '2020-03-29 14:20:36.000', 1, 'system.region.area.622924', '广河县', NULL, '622924', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3036, '2020-03-29 14:20:36.000', 1, 'system.region.area.622925', '和政县', NULL, '622925', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3037, '2020-03-29 14:20:36.000', 1, 'system.region.area.622926', '东乡族自治县', NULL, '622926', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3038, '2020-03-29 14:20:36.000', 1, 'system.region.area.622927', '积石山保安族东乡族撒拉族自治县', NULL, '622927', 30, NULL, 1, 16, 3030, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3039, '2020-03-29 14:20:36.000', 1, 'system.region.city.623000', '甘南藏族自治州', 'area', '623000', 30, NULL, 1, 15, 2947, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3040, '2020-03-29 14:20:36.000', 1, 'system.region.area.623001', '合作市', NULL, '623001', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3041, '2020-03-29 14:20:36.000', 1, 'system.region.area.623021', '临潭县', NULL, '623021', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3042, '2020-03-29 14:20:36.000', 1, 'system.region.area.623022', '卓尼县', NULL, '623022', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3043, '2020-03-29 14:20:36.000', 1, 'system.region.area.623023', '舟曲县', NULL, '623023', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3044, '2020-03-29 14:20:36.000', 1, 'system.region.area.623024', '迭部县', NULL, '623024', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3045, '2020-03-29 14:20:36.000', 1, 'system.region.area.623025', '玛曲县', NULL, '623025', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3046, '2020-03-29 14:20:36.000', 1, 'system.region.area.623026', '碌曲县', NULL, '623026', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3047, '2020-03-29 14:20:36.000', 1, 'system.region.area.623027', '夏河县', NULL, '623027', 30, NULL, 1, 16, 3039, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3048, '2020-03-29 14:20:36.000', 1, 'system.region.province.630000', '青海省', 'city', '630000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3049, '2020-03-29 14:20:36.000', 1, 'system.region.city.630100', '西宁市', 'area', '630100', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3050, '2020-03-29 14:20:36.000', 1, 'system.region.area.630102', '城东区', NULL, '630102', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3051, '2020-03-29 14:20:36.000', 1, 'system.region.area.630103', '城中区', NULL, '630103', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3052, '2020-03-29 14:20:36.000', 1, 'system.region.area.630104', '城西区', NULL, '630104', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3053, '2020-03-29 14:20:36.000', 1, 'system.region.area.630105', '城北区', NULL, '630105', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3054, '2020-03-29 14:20:36.000', 1, 'system.region.area.630106', '湟中区', NULL, '630106', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3055, '2020-03-29 14:20:36.000', 1, 'system.region.area.630121', '大通回族土族自治县', NULL, '630121', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3056, '2020-03-29 14:20:36.000', 1, 'system.region.area.630123', '湟源县', NULL, '630123', 30, NULL, 1, 16, 3049, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3057, '2020-03-29 14:20:36.000', 1, 'system.region.city.630200', '海东市', 'area', '630200', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3058, '2020-03-29 14:20:36.000', 1, 'system.region.area.630202', '乐都区', NULL, '630202', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3059, '2020-03-29 14:20:36.000', 1, 'system.region.area.630203', '平安区', NULL, '630203', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3060, '2020-03-29 14:20:36.000', 1, 'system.region.area.630222', '民和回族土族自治县', NULL, '630222', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3061, '2020-03-29 14:20:36.000', 1, 'system.region.area.630223', '互助土族自治县', NULL, '630223', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3062, '2020-03-29 14:20:36.000', 1, 'system.region.area.630224', '化隆回族自治县', NULL, '630224', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3063, '2020-03-29 14:20:36.000', 1, 'system.region.area.630225', '循化撒拉族自治县', NULL, '630225', 30, NULL, 1, 16, 3057, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3064, '2020-03-29 14:20:36.000', 1, 'system.region.city.632200', '海北藏族自治州', 'area', '632200', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3065, '2020-03-29 14:20:36.000', 1, 'system.region.area.632221', '门源回族自治县', NULL, '632221', 30, NULL, 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3066, '2020-03-29 14:20:36.000', 1, 'system.region.area.632222', '祁连县', NULL, '632222', 30, NULL, 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3067, '2020-03-29 14:20:36.000', 1, 'system.region.area.632223', '海晏县', NULL, '632223', 30, NULL, 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3068, '2020-03-29 14:20:36.000', 1, 'system.region.area.632224', '刚察县', NULL, '632224', 30, NULL, 1, 16, 3064, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3069, '2020-03-29 14:20:36.000', 1, 'system.region.city.632300', '黄南藏族自治州', 'area', '632300', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3070, '2020-03-29 14:20:36.000', 1, 'system.region.area.632321', '同仁县', NULL, '632321', 30, NULL, 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3071, '2020-03-29 14:20:36.000', 1, 'system.region.area.632322', '尖扎县', NULL, '632322', 30, NULL, 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3072, '2020-03-29 14:20:36.000', 1, 'system.region.area.632323', '泽库县', NULL, '632323', 30, NULL, 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3073, '2020-03-29 14:20:36.000', 1, 'system.region.area.632324', '河南蒙古族自治县', NULL, '632324', 30, NULL, 1, 16, 3069, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3074, '2020-03-29 14:20:36.000', 1, 'system.region.city.632500', '海南藏族自治州', 'area', '632500', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3075, '2020-03-29 14:20:36.000', 1, 'system.region.area.632521', '共和县', NULL, '632521', 30, NULL, 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3076, '2020-03-29 14:20:36.000', 1, 'system.region.area.632522', '同德县', NULL, '632522', 30, NULL, 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3077, '2020-03-29 14:20:36.000', 1, 'system.region.area.632523', '贵德县', NULL, '632523', 30, NULL, 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3078, '2020-03-29 14:20:36.000', 1, 'system.region.area.632524', '兴海县', NULL, '632524', 30, NULL, 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3079, '2020-03-29 14:20:36.000', 1, 'system.region.area.632525', '贵南县', NULL, '632525', 30, NULL, 1, 16, 3074, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3080, '2020-03-29 14:20:36.000', 1, 'system.region.city.632600', '果洛藏族自治州', 'area', '632600', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3081, '2020-03-29 14:20:36.000', 1, 'system.region.area.632621', '玛沁县', NULL, '632621', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3082, '2020-03-29 14:20:36.000', 1, 'system.region.area.632622', '班玛县', NULL, '632622', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3083, '2020-03-29 14:20:36.000', 1, 'system.region.area.632623', '甘德县', NULL, '632623', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3084, '2020-03-29 14:20:36.000', 1, 'system.region.area.632624', '达日县', NULL, '632624', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3085, '2020-03-29 14:20:36.000', 1, 'system.region.area.632625', '久治县', NULL, '632625', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3086, '2020-03-29 14:20:36.000', 1, 'system.region.area.632626', '玛多县', NULL, '632626', 30, NULL, 1, 16, 3080, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3087, '2020-03-29 14:20:36.000', 1, 'system.region.city.632700', '玉树藏族自治州', 'area', '632700', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3088, '2020-03-29 14:20:36.000', 1, 'system.region.area.632701', '玉树市', NULL, '632701', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3089, '2020-03-29 14:20:36.000', 1, 'system.region.area.632722', '杂多县', NULL, '632722', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3090, '2020-03-29 14:20:36.000', 1, 'system.region.area.632723', '称多县', NULL, '632723', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3091, '2020-03-29 14:20:36.000', 1, 'system.region.area.632724', '治多县', NULL, '632724', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3092, '2020-03-29 14:20:36.000', 1, 'system.region.area.632725', '囊谦县', NULL, '632725', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3093, '2020-03-29 14:20:36.000', 1, 'system.region.area.632726', '曲麻莱县', NULL, '632726', 30, NULL, 1, 16, 3087, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3094, '2020-03-29 14:20:36.000', 1, 'system.region.city.632800', '海西蒙古族藏族自治州', 'area', '632800', 30, NULL, 1, 15, 3048, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3095, '2020-03-29 14:20:36.000', 1, 'system.region.area.632801', '格尔木市', NULL, '632801', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3096, '2020-03-29 14:20:36.000', 1, 'system.region.area.632802', '德令哈市', NULL, '632802', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3097, '2020-03-29 14:20:36.000', 1, 'system.region.area.632803', '茫崖市', NULL, '632803', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3098, '2020-03-29 14:20:36.000', 1, 'system.region.area.632821', '乌兰县', NULL, '632821', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3099, '2020-03-29 14:20:36.000', 1, 'system.region.area.632822', '都兰县', NULL, '632822', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3100, '2020-03-29 14:20:36.000', 1, 'system.region.area.632823', '天峻县', NULL, '632823', 30, NULL, 1, 16, 3094, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3101, '2020-03-29 14:20:36.000', 1, 'system.region.province.640000', '宁夏回族自治区', 'city', '640000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3102, '2020-03-29 14:20:36.000', 1, 'system.region.city.640100', '银川市', 'area', '640100', 30, NULL, 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3103, '2020-03-29 14:20:36.000', 1, 'system.region.area.640104', '兴庆区', NULL, '640104', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3104, '2020-03-29 14:20:36.000', 1, 'system.region.area.640105', '西夏区', NULL, '640105', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3105, '2020-03-29 14:20:36.000', 1, 'system.region.area.640106', '金凤区', NULL, '640106', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3106, '2020-03-29 14:20:36.000', 1, 'system.region.area.640121', '永宁县', NULL, '640121', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3107, '2020-03-29 14:20:36.000', 1, 'system.region.area.640122', '贺兰县', NULL, '640122', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3108, '2020-03-29 14:20:36.000', 1, 'system.region.area.640181', '灵武市', NULL, '640181', 30, NULL, 1, 16, 3102, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3109, '2020-03-29 14:20:36.000', 1, 'system.region.city.640200', '石嘴山市', 'area', '640200', 30, NULL, 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3110, '2020-03-29 14:20:36.000', 1, 'system.region.area.640202', '大武口区', NULL, '640202', 30, NULL, 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3111, '2020-03-29 14:20:36.000', 1, 'system.region.area.640205', '惠农区', NULL, '640205', 30, NULL, 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3112, '2020-03-29 14:20:36.000', 1, 'system.region.area.640221', '平罗县', NULL, '640221', 30, NULL, 1, 16, 3109, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3113, '2020-03-29 14:20:36.000', 1, 'system.region.city.640300', '吴忠市', 'area', '640300', 30, NULL, 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3114, '2020-03-29 14:20:36.000', 1, 'system.region.area.640302', '利通区', NULL, '640302', 30, NULL, 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3115, '2020-03-29 14:20:36.000', 1, 'system.region.area.640303', '红寺堡区', NULL, '640303', 30, NULL, 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3116, '2020-03-29 14:20:36.000', 1, 'system.region.area.640323', '盐池县', NULL, '640323', 30, NULL, 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3117, '2020-03-29 14:20:36.000', 1, 'system.region.area.640324', '同心县', NULL, '640324', 30, NULL, 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3118, '2020-03-29 14:20:36.000', 1, 'system.region.area.640381', '青铜峡市', NULL, '640381', 30, NULL, 1, 16, 3113, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3119, '2020-03-29 14:20:36.000', 1, 'system.region.city.640400', '固原市', 'area', '640400', 30, NULL, 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3120, '2020-03-29 14:20:36.000', 1, 'system.region.area.640402', '原州区', NULL, '640402', 30, NULL, 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3121, '2020-03-29 14:20:36.000', 1, 'system.region.area.640422', '西吉县', NULL, '640422', 30, NULL, 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3122, '2020-03-29 14:20:36.000', 1, 'system.region.area.640423', '隆德县', NULL, '640423', 30, NULL, 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3123, '2020-03-29 14:20:36.000', 1, 'system.region.area.640424', '泾源县', NULL, '640424', 30, NULL, 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3124, '2020-03-29 14:20:36.000', 1, 'system.region.area.640425', '彭阳县', NULL, '640425', 30, NULL, 1, 16, 3119, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3125, '2020-03-29 14:20:36.000', 1, 'system.region.city.640500', '中卫市', 'area', '640500', 30, NULL, 1, 15, 3101, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3126, '2020-03-29 14:20:36.000', 1, 'system.region.area.640502', '沙坡头区', NULL, '640502', 30, NULL, 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3127, '2020-03-29 14:20:36.000', 1, 'system.region.area.640521', '中宁县', NULL, '640521', 30, NULL, 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3128, '2020-03-29 14:20:36.000', 1, 'system.region.area.640522', '海原县', NULL, '640522', 30, NULL, 1, 16, 3125, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3129, '2020-03-29 14:20:36.000', 1, 'system.region.province.650000', '新疆维吾尔自治区', 'city', '650000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3130, '2020-03-29 14:20:36.000', 1, 'system.region.city.650100', '乌鲁木齐市', 'area', '650100', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3131, '2020-03-29 14:20:36.000', 1, 'system.region.area.650102', '天山区', NULL, '650102', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3132, '2020-03-29 14:20:36.000', 1, 'system.region.area.650103', '沙依巴克区', NULL, '650103', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3133, '2020-03-29 14:20:36.000', 1, 'system.region.area.650104', '新市区', NULL, '650104', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3134, '2020-03-29 14:20:36.000', 1, 'system.region.area.650105', '水磨沟区', NULL, '650105', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3135, '2020-03-29 14:20:36.000', 1, 'system.region.area.650106', '头屯河区', NULL, '650106', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3136, '2020-03-29 14:20:36.000', 1, 'system.region.area.650107', '达坂城区', NULL, '650107', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3137, '2020-03-29 14:20:36.000', 1, 'system.region.area.650109', '米东区', NULL, '650109', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3138, '2020-03-29 14:20:36.000', 1, 'system.region.area.650121', '乌鲁木齐县', NULL, '650121', 30, NULL, 1, 16, 3130, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3139, '2020-03-29 14:20:36.000', 1, 'system.region.city.650200', '克拉玛依市', 'area', '650200', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3140, '2020-03-29 14:20:36.000', 1, 'system.region.area.650202', '独山子区', NULL, '650202', 30, NULL, 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3141, '2020-03-29 14:20:36.000', 1, 'system.region.area.650203', '克拉玛依区', NULL, '650203', 30, NULL, 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3142, '2020-03-29 14:20:36.000', 1, 'system.region.area.650204', '白碱滩区', NULL, '650204', 30, NULL, 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3143, '2020-03-29 14:20:36.000', 1, 'system.region.area.650205', '乌尔禾区', NULL, '650205', 30, NULL, 1, 16, 3139, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3144, '2020-03-29 14:20:36.000', 1, 'system.region.city.650400', '吐鲁番市', 'area', '650400', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3145, '2020-03-29 14:20:36.000', 1, 'system.region.area.650402', '高昌区', NULL, '650402', 30, NULL, 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3146, '2020-03-29 14:20:36.000', 1, 'system.region.area.650421', '鄯善县', NULL, '650421', 30, NULL, 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3147, '2020-03-29 14:20:36.000', 1, 'system.region.area.650422', '托克逊县', NULL, '650422', 30, NULL, 1, 16, 3144, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3148, '2020-03-29 14:20:36.000', 1, 'system.region.city.650500', '哈密市', 'area', '650500', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3149, '2020-03-29 14:20:36.000', 1, 'system.region.area.650502', '伊州区', NULL, '650502', 30, NULL, 1, 16, 3148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3150, '2020-03-29 14:20:36.000', 1, 'system.region.area.650521', '巴里坤哈萨克自治县', NULL, '650521', 30, NULL, 1, 16, 3148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3151, '2020-03-29 14:20:36.000', 1, 'system.region.area.650522', '伊吾县', NULL, '650522', 30, NULL, 1, 16, 3148, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3152, '2020-03-29 14:20:36.000', 1, 'system.region.city.652300', '昌吉回族自治州', 'area', '652300', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3153, '2020-03-29 14:20:36.000', 1, 'system.region.area.652301', '昌吉市', NULL, '652301', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3154, '2020-03-29 14:20:36.000', 1, 'system.region.area.652302', '阜康市', NULL, '652302', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3155, '2020-03-29 14:20:36.000', 1, 'system.region.area.652323', '呼图壁县', NULL, '652323', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3156, '2020-03-29 14:20:36.000', 1, 'system.region.area.652324', '玛纳斯县', NULL, '652324', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3157, '2020-03-29 14:20:36.000', 1, 'system.region.area.652325', '奇台县', NULL, '652325', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3158, '2020-03-29 14:20:36.000', 1, 'system.region.area.652327', '吉木萨尔县', NULL, '652327', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3159, '2020-03-29 14:20:36.000', 1, 'system.region.area.652328', '木垒哈萨克自治县', NULL, '652328', 30, NULL, 1, 16, 3152, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3160, '2020-03-29 14:20:36.000', 1, 'system.region.city.652700', '博尔塔拉蒙古自治州', 'area', '652700', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3161, '2020-03-29 14:20:36.000', 1, 'system.region.area.652701', '博乐市', NULL, '652701', 30, NULL, 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3162, '2020-03-29 14:20:36.000', 1, 'system.region.area.652702', '阿拉山口市', NULL, '652702', 30, NULL, 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3163, '2020-03-29 14:20:36.000', 1, 'system.region.area.652722', '精河县', NULL, '652722', 30, NULL, 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3164, '2020-03-29 14:20:36.000', 1, 'system.region.area.652723', '温泉县', NULL, '652723', 30, NULL, 1, 16, 3160, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3165, '2020-03-29 14:20:36.000', 1, 'system.region.city.652800', '巴音郭楞蒙古自治州', 'area', '652800', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3166, '2020-03-29 14:20:36.000', 1, 'system.region.area.652801', '库尔勒市', NULL, '652801', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3167, '2020-03-29 14:20:36.000', 1, 'system.region.area.652822', '轮台县', NULL, '652822', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3168, '2020-03-29 14:20:36.000', 1, 'system.region.area.652823', '尉犁县', NULL, '652823', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3169, '2020-03-29 14:20:36.000', 1, 'system.region.area.652824', '若羌县', NULL, '652824', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3170, '2020-03-29 14:20:36.000', 1, 'system.region.area.652825', '且末县', NULL, '652825', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3171, '2020-03-29 14:20:36.000', 1, 'system.region.area.652826', '焉耆回族自治县', NULL, '652826', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3172, '2020-03-29 14:20:36.000', 1, 'system.region.area.652827', '和静县', NULL, '652827', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3173, '2020-03-29 14:20:36.000', 1, 'system.region.area.652828', '和硕县', NULL, '652828', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3174, '2020-03-29 14:20:36.000', 1, 'system.region.area.652829', '博湖县', NULL, '652829', 30, NULL, 1, 16, 3165, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3175, '2020-03-29 14:20:36.000', 1, 'system.region.city.652900', '阿克苏地区', 'area', '652900', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3176, '2020-03-29 14:20:36.000', 1, 'system.region.area.652901', '阿克苏市', NULL, '652901', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3177, '2020-03-29 14:20:36.000', 1, 'system.region.area.652902', '库车市', NULL, '652902', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3178, '2020-03-29 14:20:36.000', 1, 'system.region.area.652922', '温宿县', NULL, '652922', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3179, '2020-03-29 14:20:36.000', 1, 'system.region.area.652924', '沙雅县', NULL, '652924', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3180, '2020-03-29 14:20:36.000', 1, 'system.region.area.652925', '新和县', NULL, '652925', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3181, '2020-03-29 14:20:36.000', 1, 'system.region.area.652926', '拜城县', NULL, '652926', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3182, '2020-03-29 14:20:36.000', 1, 'system.region.area.652927', '乌什县', NULL, '652927', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3183, '2020-03-29 14:20:36.000', 1, 'system.region.area.652928', '阿瓦提县', NULL, '652928', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3184, '2020-03-29 14:20:36.000', 1, 'system.region.area.652929', '柯坪县', NULL, '652929', 30, NULL, 1, 16, 3175, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3185, '2020-03-29 14:20:36.000', 1, 'system.region.city.653000', '克孜勒苏柯尔克孜自治州', 'area', '653000', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3186, '2020-03-29 14:20:36.000', 1, 'system.region.area.653001', '阿图什市', NULL, '653001', 30, NULL, 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3187, '2020-03-29 14:20:36.000', 1, 'system.region.area.653022', '阿克陶县', NULL, '653022', 30, NULL, 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3188, '2020-03-29 14:20:36.000', 1, 'system.region.area.653023', '阿合奇县', NULL, '653023', 30, NULL, 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3189, '2020-03-29 14:20:36.000', 1, 'system.region.area.653024', '乌恰县', NULL, '653024', 30, NULL, 1, 16, 3185, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3190, '2020-03-29 14:20:36.000', 1, 'system.region.city.653100', '喀什地区', 'area', '653100', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3191, '2020-03-29 14:20:36.000', 1, 'system.region.area.653101', '喀什市', NULL, '653101', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3192, '2020-03-29 14:20:36.000', 1, 'system.region.area.653121', '疏附县', NULL, '653121', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3193, '2020-03-29 14:20:36.000', 1, 'system.region.area.653122', '疏勒县', NULL, '653122', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3194, '2020-03-29 14:20:36.000', 1, 'system.region.area.653123', '英吉沙县', NULL, '653123', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3195, '2020-03-29 14:20:36.000', 1, 'system.region.area.653124', '泽普县', NULL, '653124', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3196, '2020-03-29 14:20:36.000', 1, 'system.region.area.653125', '莎车县', NULL, '653125', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3197, '2020-03-29 14:20:36.000', 1, 'system.region.area.653126', '叶城县', NULL, '653126', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3198, '2020-03-29 14:20:36.000', 1, 'system.region.area.653127', '麦盖提县', NULL, '653127', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3199, '2020-03-29 14:20:36.000', 1, 'system.region.area.653128', '岳普湖县', NULL, '653128', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3200, '2020-03-29 14:20:36.000', 1, 'system.region.area.653129', '伽师县', NULL, '653129', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3201, '2020-03-29 14:20:36.000', 1, 'system.region.area.653130', '巴楚县', NULL, '653130', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3202, '2020-03-29 14:20:36.000', 1, 'system.region.area.653131', '塔什库尔干塔吉克自治县', NULL, '653131', 30, NULL, 1, 16, 3190, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3203, '2020-03-29 14:20:36.000', 1, 'system.region.city.653200', '和田地区', 'area', '653200', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3204, '2020-03-29 14:20:36.000', 1, 'system.region.area.653201', '和田市', NULL, '653201', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3205, '2020-03-29 14:20:36.000', 1, 'system.region.area.653221', '和田县', NULL, '653221', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3206, '2020-03-29 14:20:36.000', 1, 'system.region.area.653222', '墨玉县', NULL, '653222', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3207, '2020-03-29 14:20:36.000', 1, 'system.region.area.653223', '皮山县', NULL, '653223', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3208, '2020-03-29 14:20:36.000', 1, 'system.region.area.653224', '洛浦县', NULL, '653224', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3209, '2020-03-29 14:20:36.000', 1, 'system.region.area.653225', '策勒县', NULL, '653225', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3210, '2020-03-29 14:20:36.000', 1, 'system.region.area.653226', '于田县', NULL, '653226', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3211, '2020-03-29 14:20:36.000', 1, 'system.region.area.653227', '民丰县', NULL, '653227', 30, NULL, 1, 16, 3203, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3212, '2020-03-29 14:20:36.000', 1, 'system.region.city.654000', '伊犁哈萨克自治州', 'area', '654000', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3213, '2020-03-29 14:20:36.000', 1, 'system.region.area.654002', '伊宁市', NULL, '654002', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3214, '2020-03-29 14:20:36.000', 1, 'system.region.area.654003', '奎屯市', NULL, '654003', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3215, '2020-03-29 14:20:36.000', 1, 'system.region.area.654004', '霍尔果斯市', NULL, '654004', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3216, '2020-03-29 14:20:36.000', 1, 'system.region.area.654021', '伊宁县', NULL, '654021', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3217, '2020-03-29 14:20:36.000', 1, 'system.region.area.654022', '察布查尔锡伯自治县', NULL, '654022', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3218, '2020-03-29 14:20:36.000', 1, 'system.region.area.654023', '霍城县', NULL, '654023', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3219, '2020-03-29 14:20:36.000', 1, 'system.region.area.654024', '巩留县', NULL, '654024', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3220, '2020-03-29 14:20:36.000', 1, 'system.region.area.654025', '新源县', NULL, '654025', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3221, '2020-03-29 14:20:36.000', 1, 'system.region.area.654026', '昭苏县', NULL, '654026', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3222, '2020-03-29 14:20:36.000', 1, 'system.region.area.654027', '特克斯县', NULL, '654027', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3223, '2020-03-29 14:20:36.000', 1, 'system.region.area.654028', '尼勒克县', NULL, '654028', 30, NULL, 1, 16, 3212, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3224, '2020-03-29 14:20:36.000', 1, 'system.region.city.654200', '塔城地区', 'area', '654200', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3225, '2020-03-29 14:20:36.000', 1, 'system.region.area.654201', '塔城市', NULL, '654201', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3226, '2020-03-29 14:20:36.000', 1, 'system.region.area.654202', '乌苏市', NULL, '654202', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3227, '2020-03-29 14:20:36.000', 1, 'system.region.area.654221', '额敏县', NULL, '654221', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3228, '2020-03-29 14:20:36.000', 1, 'system.region.area.654223', '沙湾县', NULL, '654223', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3229, '2020-03-29 14:20:36.000', 1, 'system.region.area.654224', '托里县', NULL, '654224', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3230, '2020-03-29 14:20:36.000', 1, 'system.region.area.654225', '裕民县', NULL, '654225', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3231, '2020-03-29 14:20:36.000', 1, 'system.region.area.654226', '和布克赛尔蒙古自治县', NULL, '654226', 30, NULL, 1, 16, 3224, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3232, '2020-03-29 14:20:36.000', 1, 'system.region.city.654300', '阿勒泰地区', 'area', '654300', 30, NULL, 1, 15, 3129, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3233, '2020-03-29 14:20:36.000', 1, 'system.region.area.654301', '阿勒泰市', NULL, '654301', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3234, '2020-03-29 14:20:36.000', 1, 'system.region.area.654321', '布尔津县', NULL, '654321', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3235, '2020-03-29 14:20:36.000', 1, 'system.region.area.654322', '富蕴县', NULL, '654322', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3236, '2020-03-29 14:20:36.000', 1, 'system.region.area.654323', '福海县', NULL, '654323', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3237, '2020-03-29 14:20:36.000', 1, 'system.region.area.654324', '哈巴河县', NULL, '654324', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3238, '2020-03-29 14:20:36.000', 1, 'system.region.area.654325', '青河县', NULL, '654325', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3239, '2020-03-29 14:20:36.000', 1, 'system.region.area.654326', '吉木乃县', NULL, '654326', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3240, '2020-03-29 14:20:36.000', 1, 'system.region.area.659001', '石河子市', NULL, '659001', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3241, '2020-03-29 14:20:36.000', 1, 'system.region.area.659002', '阿拉尔市', NULL, '659002', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3242, '2020-03-29 14:20:36.000', 1, 'system.region.area.659003', '图木舒克市', NULL, '659003', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3243, '2020-03-29 14:20:36.000', 1, 'system.region.area.659004', '五家渠市', NULL, '659004', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3244, '2020-03-29 14:20:36.000', 1, 'system.region.area.659005', '北屯市', NULL, '659005', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3245, '2020-03-29 14:20:36.000', 1, 'system.region.area.659006', '铁门关市', NULL, '659006', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3246, '2020-03-29 14:20:36.000', 1, 'system.region.area.659007', '双河市', NULL, '659007', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3247, '2020-03-29 14:20:36.000', 1, 'system.region.area.659008', '可克达拉市', NULL, '659008', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3248, '2020-03-29 14:20:36.000', 1, 'system.region.area.659009', '昆玉市', NULL, '659009', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3249, '2020-03-29 14:20:36.000', 1, 'system.region.area.659010', '胡杨河市', NULL, '659010', 30, NULL, 1, 16, 3232, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3250, '2020-03-29 14:20:36.000', 1, 'system.region.province.710000', '台湾省', NULL, '710000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3251, '2020-03-29 14:20:36.000', 1, 'system.region.province.810000', '香港特别行政区', NULL, '810000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3252, '2020-03-29 14:20:36.000', 1, 'system.region.province.820000', '澳门特别行政区', NULL, '820000', 30, NULL, 1, 14, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3253, '2025-04-06 11:38:18.178', 1, 'system.crypto.access.type.server', '服务端加解密', NULL, 'server', 30, NULL, 1, 5, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3254, '2025-04-06 11:38:18.187', 1, 'system.crypto.access.type.mobile', '移动端加解密', NULL, 'mobile', 30, NULL, 1, 5, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3255, '2025-04-06 11:38:18.194', 1, 'system.crypto.access.predicate.after', '时间之后', NULL, 'After', 30, NULL, 1, 4, NULL, NULL, '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之后发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` VALUES (3256, '2025-04-06 11:38:18.201', 1, 'system.crypto.access.predicate.before', '时间之前', NULL, 'Before', 30, NULL, 1, 4, NULL, NULL, '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之前发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` VALUES (3257, '2025-04-06 11:38:18.207', 1, 'system.crypto.access.predicate.between', '时间范围', NULL, 'Between', 30, NULL, 1, 4, NULL, NULL, '在该日期时间范围发生的请求都将被匹配，如：datetime1=2020-01-20T17:42:47.789 datetime1=2020-03-20T17:42:47.789，在 2020-01-20 17:42:47 到 2020-03-20T17:42:47 发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` VALUES (3258, '2025-04-06 11:38:18.213', 1, 'system.crypto.access.predicate.cookie', '请求Cookie匹配', NULL, 'Cookie', 30, NULL, 1, 4, NULL, NULL, '请求 Cookie 匹配，如：name=chocolate regexp=ch.p，表示 cookei 存在 chocolate 并且正则表达式对条件 ch.p 通过则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3259, '2025-04-06 11:38:18.219', 1, 'system.crypto.access.predicate.header', '请求头匹配', NULL, 'Header', 30, NULL, 1, 4, NULL, NULL, '请求头匹配，如：name=X-REQUST-ID regexp=d+，表示 header 存在 X-REQUST-ID 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3260, '2025-04-06 11:38:18.226', 1, 'system.crypto.access.predicate.host', '访问主机匹配', NULL, 'Host', 30, NULL, 1, 4, NULL, NULL, '访问主机匹配，如：patterns=**.somehost.org,**.anotherhost.org，表示访问来源是 somehost.org 或 **.anotherhost.org 时则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3261, '2025-04-06 11:38:18.233', 1, 'system.crypto.access.predicate.method', '请求方法匹配', NULL, 'Method', 30, NULL, 1, 4, NULL, NULL, '请求方法匹配，如：methods=POST,GET，表示请求是 POST 或 GET，表示请求是 时则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3262, '2025-04-06 11:38:18.241', 1, 'system.crypto.access.predicate.path', '请求路径匹配', NULL, 'Path', 30, NULL, 1, 4, NULL, NULL, '请求路径匹配，如：patterns=/foo/**,/bar/**，表示请求路径是带有/foo/前缀 或 /bar/前缀时则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3263, '2025-04-06 11:38:18.249', 1, 'system.crypto.access.predicate.query', '请求参数匹配', NULL, 'Query', 30, NULL, 1, 4, NULL, NULL, '请求参数匹配，如：param=id regexp=d+，表示请求参数是 id 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3264, '2025-04-06 11:38:18.255', 1, 'system.crypto.access.predicate.remote-address', '访问IP匹配', NULL, 'RemoteAddr', 30, NULL, 1, 4, NULL, NULL, '访问IP匹配，如：sources=192.168.0.1/24,192.168.6.1/24 表示只有访问 IP 在 192.168.0.[1到24] 或 192.168.6.[1到24] 时则匹配');
INSERT INTO `tb_data_dictionary` VALUES (3265, '2025-04-06 11:38:18.262', 1, 'system.crypto.algorithm.padding-scheme.none', '无', NULL, 'NONE', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3266, '2025-04-06 11:38:18.268', 1, 'system.crypto.algorithm.padding-scheme.iso10126', 'ISO10126Padding', NULL, 'ISO10126', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3267, '2025-04-06 11:38:18.273', 1, 'system.crypto.algorithm.padding-scheme.oaep', 'OAEPPadding', NULL, 'OAEP', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3268, '2025-04-06 11:38:18.279', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-md5-and-mgf1', 'OAEPWithMD5AndMGF1Padding', NULL, 'OAEPWithMd5AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3269, '2025-04-06 11:38:18.285', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha1-and-mgf1', 'OAEPWithSHA-1AndMGF1Padding', NULL, 'OAEPWithSha1AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3270, '2025-04-06 11:38:18.290', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha-384-and-mgf1', 'OAEPWithSHA-384AndMGF1Padding', NULL, 'OAEPWithSha384AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3271, '2025-04-06 11:38:18.296', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha-512-and-mgf1', 'OAEPWithSHA-512AndMGF1Padding', NULL, 'OAEPWithSha512AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3272, '2025-04-06 11:38:18.302', 1, 'system.crypto.algorithm.padding-scheme.pkcs1', 'PKCS1Padding', NULL, 'PKCS1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3273, '2025-04-06 11:38:18.308', 1, 'system.crypto.algorithm.padding-scheme.pkcs5', 'PKCS5Padding', NULL, 'PKCS5', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3274, '2025-04-06 11:38:18.312', 1, 'system.crypto.algorithm.padding-scheme.ssl3', 'SSL3Padding', NULL, 'SSL3', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3275, '2025-04-06 11:38:18.317', 1, 'system.crypto.algorithm.mode.none', '无', NULL, 'NONE', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3276, '2025-04-06 11:38:18.323', 1, 'system.crypto.algorithm.mode.cbc', 'CBC', NULL, 'CBC', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3277, '2025-04-06 11:38:18.328', 1, 'system.crypto.algorithm.mode.cfb', 'CFB', NULL, 'CFB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3278, '2025-04-06 11:38:18.332', 1, 'system.crypto.algorithm.mode.ctr', 'CTR', NULL, 'CTR', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3279, '2025-04-06 11:38:18.337', 1, 'system.crypto.algorithm.mode.ecb', 'ECB', NULL, 'ECB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3280, '2025-04-06 11:38:18.342', 1, 'system.crypto.algorithm.mode.ofb', 'OFB', NULL, 'OFB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3281, '2025-04-06 11:38:18.348', 1, 'system.crypto.algorithm.mode.pcbc', 'PCBC', NULL, 'PCBC', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3283, '2025-04-06 11:38:18.358', 1, 'system.email.captcha.bind', '绑定邮箱', NULL, '<!DOCTYPE html>\r\n<html lang=\"zh-CN\">\r\n<head>\r\n    <meta charset=\"UTF-8\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n    <title>${operation}验证码</title>\r\n</head>\r\n<body style=\"margin:0; padding:0; background-color:#f5f5f5; height:100%; width:100%;\">\r\n\r\n    <table role=\"presentation\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f5f5f5;margin-top: 5%\">\r\n        <tr>\r\n            <td align=\"center\" valign=\"middle\">\r\n                <table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#ffffff; border-radius:8px; padding:40px;\">\r\n                    <tr>\r\n                        <td align=\"center\" style=\"padding-bottom: 20px;\">\r\n                            <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\r\n                                <tr>\r\n                                    <td valign=\"middle\" style=\"padding-right: 10px;\">\r\n                                        <img style=\"width: 50px;height:50px;\" src=\"data:image/png;base64,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\" alt=\"image\">\r\n                                    </td>\r\n                                    <td valign=\"middle\">\r\n                                        <span style=\"font-family: Arial, sans-serif; font-size: 20px; color: #333333; line-height: 1.2;\">Basic Admin</span>\r\n                                    </td>\r\n                                </tr>\r\n                            </table>\r\n\r\n                        </td>\r\n                    </tr>\r\n                    <tr>\r\n                        <td align=\"left\">\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">尊敬的用户：</p>\r\n\r\n                            <p style=\"margin:15px 0 0 0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">\r\n                                您正在进行 <strong>${operation}</strong> 操作，本次验证码为：\r\n                            </p>\r\n\r\n                            <p style=\"margin:20px 0; font-family:Arial, sans-serif; text-align:center;\">\r\n                                <span style=\"font-size:32px; color:#003ba5; font-weight:bold; letter-spacing:2px;\">${code}</span>\r\n                            </p>\r\n\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:14px; color:#666666; line-height:1.6;\">\r\n                                验证码的有效期是 <strong>${expireTime}</strong>，请您尽快操作。请勿泄露验证码给到他人，以免出现安全隐患。\r\n                            </p>\r\n\r\n                            <p style=\"margin:30px 0 0 0; font-family:Arial, sans-serif; font-size:14px; color:#999999; text-align:right;\">\r\n                                loncra basic admin\r\n                            </p>\r\n                        </td>\r\n                    </tr>\r\n                </table>\r\n\r\n            </td>\r\n        </tr>\r\n    </table>\r\n\r\n</body>\r\n</html>', 30, NULL, 1, 49, NULL, 0, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3284, '2025-04-06 11:38:18.365', 1, 'system.email.captcha.unbind', '解绑邮箱', NULL, '<!DOCTYPE html>\r\n<html lang=\"zh-CN\">\r\n<head>\r\n    <meta charset=\"UTF-8\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n    <title>${operation}验证码</title>\r\n</head>\r\n<body style=\"margin:0; padding:0; background-color:#f5f5f5; height:100%; width:100%;\">\r\n\r\n    <table role=\"presentation\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f5f5f5;margin-top: 5%\">\r\n        <tr>\r\n            <td align=\"center\" valign=\"middle\">\r\n                <table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#ffffff; border-radius:8px; padding:40px;\">\r\n                    <tr>\r\n                        <td align=\"center\" style=\"padding-bottom: 20px;\">\r\n                            <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\r\n                                <tr>\r\n                                    <td valign=\"middle\" style=\"padding-right: 10px;\">\r\n                                        <img style=\"width: 50px;height:50px;\" src=\"data:image/png;base64,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\" alt=\"image\">\r\n                                    </td>\r\n                                    <td valign=\"middle\">\r\n                                        <span style=\"font-family: Arial, sans-serif; font-size: 20px; color: #333333; line-height: 1.2;\">Basic Admin</span>\r\n                                    </td>\r\n                                </tr>\r\n                            </table>\r\n\r\n                        </td>\r\n                    </tr>\r\n                    <tr>\r\n                        <td align=\"left\">\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">尊敬的用户：</p>\r\n\r\n                            <p style=\"margin:15px 0 0 0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">\r\n                                您正在进行 <strong>${operation}</strong> 操作，本次验证码为：\r\n                            </p>\r\n\r\n                            <p style=\"margin:20px 0; font-family:Arial, sans-serif; text-align:center;\">\r\n                                <span style=\"font-size:32px; color:#003ba5; font-weight:bold; letter-spacing:2px;\">${code}</span>\r\n                            </p>\r\n\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:14px; color:#666666; line-height:1.6;\">\r\n                                验证码的有效期是 <strong>${expireTime}</strong>，请您尽快操作。请勿泄露验证码给到他人，以免出现安全隐患。\r\n                            </p>\r\n\r\n                            <p style=\"margin:30px 0 0 0; font-family:Arial, sans-serif; font-size:14px; color:#999999; text-align:right;\">\r\n                                loncra basic admin\r\n                            </p>\r\n                        </td>\r\n                    </tr>\r\n                </table>\r\n\r\n            </td>\r\n        </tr>\r\n    </table>\r\n\r\n</body>\r\n</html>', 30, NULL, 1, 49, NULL, 2, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3287, '2025-04-06 11:38:18.384', 1, 'system.email.attachment.send', '导出文件', NULL, '<div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                                <tr>\n                                    <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                        <table border=\"0\" align=\"center\" style=\"border-collapse:collapse;border-spacing:0;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                            <tbody>\n                                                <tr>\n                                                    <td style=\"width:150px;\">\n                                                           <img width=\"150\" style=\"border:none;\" src=\'https://i22g472016.zicp.fun/server/resource/attachment/query?bucketName=cloudmasses.saas.resource.system.file&objectName=skn.jpg\'/>\n                                                    </td>\n                                                </tr>\n                                            </tbody>\n                                        </table>\n                                    </td>\n                                </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n    <div style=\"max-width:640px;margin:0 auto;box-shadow:0 1px 5px rgba(0,0,0,0.1);border-radius:4px;overflow:hidden\">\n        <div style=\"margin:0 auto;max-width:640px;background:#ffffff;\">\n            <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:#ffffff;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                <tbody>\n                    <tr>\n                        <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:40px 50px;\">\n                            <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\" class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                                <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                    <tbody>\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#737F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:16px;line-height:24px;text-align:left;\">\n                                                <h2 style=\"font-family: Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-weight: 500;font-size: 20px;color: #4F545C;letter-spacing: 0.27px;\">您好!</h2>\n                                                <p>这是系统为您导出的文件数据【{0}】</p>\n                                                <p>请点击附件进行下载，保存好文件，以免邮箱附件过期。</p>\n                                            </div>\n                                        </td>\n                                    </tr>\n\n                                    <tr>\n                                        <td style=\"word-break:break-word;font-size:0;padding:30px 0px;\"><p style=\"font-size:1px;margin:0 auto;border-top:1px solid #DCDDDE;width:100%;\"></p></td>\n                                    </tr>\n\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#747F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:13px;line-height:16px;text-align:left;\">\n                                                <p>\n                                                    此邮件为系统邮件，请勿回复，如果此邮箱与你无关，请忽略。\n                                                </p>\n                                            </div>\n                                        </td>\n                                    </tr>\n                                    </tbody>\n                                </table>\n                            </div>\n                        </td>\n                    </tr>\n                </tbody>\n            </table>\n        </div>\n    </div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0px;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                            <tr>\n                                <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                    <div style=\"cursor:auto;color:#99AAB5;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:12px;line-height:24px;text-align:center;\">\n                                        COPYRIGHT © 2023 广西和湛科技有限公司, All rights ReservedHand-crafted & Made with 1.0.0\n                                    </div>\n                                </td>\n                            </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n</div> ', 30, NULL, 1, 8, NULL, 1, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3288, '2025-04-06 11:38:18.388', 2, 'system.email.captcha.forgot-password', '忘记密码', NULL, '<!DOCTYPE html>\r\n<html lang=\"zh-CN\">\r\n<head>\r\n    <meta charset=\"UTF-8\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n    <title>${operation}验证码</title>\r\n</head>\r\n<body style=\"margin:0; padding:0; background-color:#f5f5f5; height:100%; width:100%;\">\r\n\r\n    <table role=\"presentation\" width=\"100%\" height=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f5f5f5;margin-top: 5%\">\r\n        <tr>\r\n            <td align=\"center\" valign=\"middle\">\r\n                <table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#ffffff; border-radius:8px; padding:40px;\">\r\n                    <tr>\r\n                        <td align=\"center\" style=\"padding-bottom: 20px;\">\r\n                            <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\r\n                                <tr>\r\n                                    <td valign=\"middle\" style=\"padding-right: 10px;\">\r\n                                        <img style=\"width: 50px;height:50px;\" src=\"data:image/png;base64,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\" alt=\"image\">\r\n                                    </td>\r\n                                    <td valign=\"middle\">\r\n                                        <span style=\"font-family: Arial, sans-serif; font-size: 20px; color: #333333; line-height: 1.2;\">Basic Admin</span>\r\n                                    </td>\r\n                                </tr>\r\n                            </table>\r\n\r\n                        </td>\r\n                    </tr>\r\n                    <tr>\r\n                        <td align=\"left\">\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">尊敬的用户：</p>\r\n\r\n                            <p style=\"margin:15px 0 0 0; font-family:Arial, sans-serif; font-size:16px; color:#333333;\">\r\n                                您正在进行 <strong>${operation}</strong> 操作，本次验证码为：\r\n                            </p>\r\n\r\n                            <p style=\"margin:20px 0; font-family:Arial, sans-serif; text-align:center;\">\r\n                                <span style=\"font-size:32px; color:#003ba5; font-weight:bold; letter-spacing:2px;\">${code}</span>\r\n                            </p>\r\n\r\n                            <p style=\"margin:0; font-family:Arial, sans-serif; font-size:14px; color:#666666; line-height:1.6;\">\r\n                                验证码的有效期是 <strong>${expireTime}</strong>，请您尽快操作。请勿泄露验证码给到他人，以免出现安全隐患。\r\n                            </p>\r\n\r\n                            <p style=\"margin:30px 0 0 0; font-family:Arial, sans-serif; font-size:14px; color:#999999; text-align:right;\">\r\n                                loncra basic admin\r\n                            </p>\r\n                        </td>\r\n                    </tr>\r\n                </table>\r\n\r\n            </td>\r\n        </tr>\r\n    </table>\r\n\r\n</body>\r\n</html>', 30, '{}', 1, 49, NULL, 2147, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3289, '2025-04-06 11:38:18.397', 1, 'system.sms.captcha.login', '登录或注册验证码', '', '{\"templateCode\":\"SMS_496880023\",\"signCode\":\"广西网智链科技\"}', 100, '{}', 1, 10, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` VALUES (3653, '2026-06-18 11:47:29.086', 1, 'system.sms.captcha.forgot-password', '忘记密码', '', '{\"templateCode\":\"SMS_496880023\",\"signCode\":\"广西网智链科技\"}', 100, '{}', 1, 10, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for tb_dictionary_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_dictionary_type`;
CREATE TABLE `tb_dictionary_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '键名称',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '类型名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父字典类型,根节点为 null',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_type`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典类型' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_dictionary_type
-- ----------------------------
INSERT INTO `tb_dictionary_type` VALUES (1, '2026-05-16 02:02:24.628', 9, 'system', '系统配置项', NULL, '');
INSERT INTO `tb_dictionary_type` VALUES (2, '2020-03-29 13:49:01.000', 1, 'system.crypto', '加解密', 1, NULL);
INSERT INTO `tb_dictionary_type` VALUES (3, '2020-03-29 14:16:09.000', 1, 'system.crypto.access', '访问', 2, NULL);
INSERT INTO `tb_dictionary_type` VALUES (4, '2020-03-29 14:18:01.000', 1, 'system.crypto.access.predicate', '条件', 3, NULL);
INSERT INTO `tb_dictionary_type` VALUES (5, '2020-03-29 14:18:54.000', 1, 'system.crypto.access.type', '类型', 3, NULL);
INSERT INTO `tb_dictionary_type` VALUES (6, '2020-03-29 14:18:54.000', 1, 'system.crypto.algorithm.padding-scheme', '加解密算法填充方案', 3, NULL);
INSERT INTO `tb_dictionary_type` VALUES (7, '2020-03-29 14:18:54.000', 1, 'system.crypto.algorithm.mode', '加解密算法模型', 3, NULL);
INSERT INTO `tb_dictionary_type` VALUES (8, '2020-03-29 14:18:54.000', 1, 'system.email', '邮箱', 1, NULL);
INSERT INTO `tb_dictionary_type` VALUES (9, '2020-03-29 14:18:54.000', 1, 'system.sms', '短信', 1, NULL);
INSERT INTO `tb_dictionary_type` VALUES (10, '2025-03-24 18:42:43.382', 1, 'system.sms.captcha', '短信验证码', 9, '');
INSERT INTO `tb_dictionary_type` VALUES (13, '2020-03-29 14:18:54.000', 2, 'system.region', '区域', 1, NULL);
INSERT INTO `tb_dictionary_type` VALUES (14, '2021-08-16 11:21:52.873', 1, 'system.region.province', '省', 13, NULL);
INSERT INTO `tb_dictionary_type` VALUES (15, '2020-03-29 14:18:54.000', 1, 'system.region.city', '市', 13, NULL);
INSERT INTO `tb_dictionary_type` VALUES (16, '2020-03-29 14:18:54.000', 1, 'system.region.area', '县', 13, NULL);
INSERT INTO `tb_dictionary_type` VALUES (49, '2026-06-22 09:48:49.661', 1, 'system.email.captcha', '邮箱验证码', 8, NULL);

-- ----------------------------
-- Table structure for tb_email_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_email_message`;
CREATE TABLE `tb_email_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `from_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送邮件',
  `to_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收取邮件',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `retry_time` datetime(3) NULL DEFAULT NULL COMMENT '最后发送时间',
  `execute_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime(3) NULL DEFAULT NULL COMMENT '发送成功时间',
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常信息',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `has_attachment` tinyint NULL DEFAULT NULL COMMENT '是否存在附件:0.否,1.是',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '批量消息 id',
  `attachment_list` json NULL COMMENT '附件集合',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收件人',
  `metadata` json NULL COMMENT '元数据信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_from_user`(`from_email` ASC) USING BTREE,
  INDEX `ix_to_user`(`to_email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_email_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_open_platform_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_open_platform_merchant`;
CREATE TABLE `tb_open_platform_merchant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'app id',
  `app_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'aes key',
  `private_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'rsa 公共密钥',
  `public_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'rsa 私有密钥',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `enabled` tinyint NOT NULL COMMENT '是否启用:0.否, 1.是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_app_id`(`app_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '开放平台商户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_open_platform_merchant
-- ----------------------------

-- ----------------------------
-- Table structure for tb_open_platform_merchant_client
-- ----------------------------
DROP TABLE IF EXISTS `tb_open_platform_merchant_client`;
CREATE TABLE `tb_open_platform_merchant_client`  (
  `id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '主键 id',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `creation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `merchant_id` bigint NOT NULL COMMENT '商户 id',
  `client_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '客户端 id',
  `client_id_issued_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '客户端 id 发放时间',
  `client_secret` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '客户端密钥',
  `client_secret_expires_at` datetime NOT NULL COMMENT '客户端密钥过期时间',
  `client_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '客户端名称',
  `client_authentication_methods` json NOT NULL COMMENT '授权方法',
  `authorization_grant_types` json NOT NULL COMMENT '认证类型',
  `redirect_uris` json NULL COMMENT '重定向 url',
  `scopes` json NOT NULL COMMENT '授权作用域',
  `client_settings` json NOT NULL COMMENT '客户端设置',
  `token_settings` json NOT NULL COMMENT 'token 设置',
  `enabled` tinyint NOT NULL COMMENT '是否启用:0.否,1.是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_client_id`(`client_id` ASC) USING BTREE,
  UNIQUE INDEX `ux_merchant_id`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商家 OAuth 2 客户端注册信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_open_platform_merchant_client
-- ----------------------------
INSERT INTO `tb_open_platform_merchant_client` VALUES ('1', 1, '2026-03-14 20:52:05', 1, '19867271-501f-4c96-916f-d7e0a3fb7321', '2026-03-14 20:52:05', '$2a$10$n3PqiKyjQCA36NFhlHRnsua6t9HeiOWlNdQtwsH7o/E8DHOL/3VCu', '2045-03-01 20:45:01', 'basic-service', '[\"CLIENT_SECRET_BASIC\", \"CLIENT_SECRET_POST\"]', '[\"REFRESH_TOKEN\", \"AUTHORIZATION_CODE\"]', '[\"http://localhost:8080\"]', '[\"PROFILE\", \"OPENID\", \"UNIONID\", \"EMAIL\", \"ADDRESS\", \"PHONE\", \"ROLE\"]', '{\"requireProofKey\": \"No\", \"requireAuthorizationConsent\": \"Yes\", \"authorizationConsentExpirationTime\": {\"unit\": \"DAYS\", \"value\": 180}, \"tokenEndpointAuthenticationSigningAlgorithmType\": \"MAC_ALGORITHM\", \"tokenEndpointAuthenticationSigningAlgorithmValue\": \"HS256\"}', '{\"accessTokenFormat\": \"SELF_CONTAINED\", \"reuseRefreshTokens\": \"Yes\", \"accessTokenTimeToLive\": {\"unit\": \"DAYS\", \"value\": 1}, \"refreshTokenTimeToLive\": {\"unit\": \"DAYS\", \"value\": 1}, \"idTokenSignatureAlgorithm\": \"RS256\", \"authorizationCodeTimeToLive\": {\"unit\": \"MINUTES\", \"value\": 5}}', 1);

-- ----------------------------
-- Table structure for tb_enterprise
-- ----------------------------
DROP TABLE IF EXISTS `tb_enterprise`;
CREATE TABLE `tb_enterprise`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，同时作为企业空间租户 id',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '企业名称',
  `owner_principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '企业主',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用'
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_organization_owner_principal`(`owner_principal` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '企业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_enterprise
-- ----------------------------

-- ----------------------------
-- Table structure for tb_enterprise_invitation
-- ----------------------------
DROP TABLE IF EXISTS `tb_enterprise_invitation`;
CREATE TABLE `tb_enterprise_invitation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `organization_id` bigint NOT NULL COMMENT '企业 id',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '邀请码',
  `phone_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '被邀请手机号',
  `inviter_principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邀请人',
  `status` tinyint NOT NULL DEFAULT 10 COMMENT '状态:10.待接受,20.已接受,30.已过期,40.已取消',
  `expiration_time` datetime(3) NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_organization_invitation_code`(`code` ASC) USING BTREE,
  INDEX `idx_organization_invitation_organization_id`(`organization_id` ASC) USING BTREE,
  INDEX `idx_organization_invitation_phone_status`(`phone_number` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '企业邀请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_enterprise_invitation
-- ----------------------------

-- ----------------------------
-- Table structure for tb_enterprise_member
-- ----------------------------
DROP TABLE IF EXISTS `tb_enterprise_member`;
CREATE TABLE `tb_enterprise_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `organization_id` bigint NOT NULL COMMENT '企业 id',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '成员认证主体',
  `role` tinyint NOT NULL DEFAULT 30 COMMENT '角色:10.企业主,20.管理员,30.成员',
  `status` tinyint NOT NULL DEFAULT 10 COMMENT '状态:10.待加入,20.已加入,30.已禁用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_organization_member`(`organization_id` ASC, `principal` ASC) USING BTREE,
  INDEX `idx_organization_member_principal_status`(`principal` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '企业成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_enterprise_member
-- ----------------------------

-- ----------------------------
-- Table structure for tb_personal_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_personal_user`;
CREATE TABLE `tb_personal_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `password` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '登录帐号',
  `gender` tinyint NOT NULL COMMENT '性别:10.男,20.女',
  `real_name` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '真实姓名',
  `phone_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '电话号码',
  `role_ids` json NULL COMMENT '组信息',
  `resource_ids` json NULL COMMENT '资源信息',
  `last_authentication_time` datetime NULL DEFAULT NULL COMMENT '最后认证(登入)时间',
  `phone_number_verified` tinyint NULL DEFAULT 0 COMMENT '是验证码手机号码',
  `email_verified` tinyint NULL DEFAULT 0 COMMENT '是否验证邮箱',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `initialization` json NOT NULL COMMENT '用户初始化信息',
  `promo_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '推荐人',
  `tenant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '个人空间租户 id',
  `last_active_organization_id` bigint NULL DEFAULT NULL COMMENT '上次使用的企业 id，为空表示个人空间',
  `avatar` json NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '个人用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_personal_user
-- ----------------------------

-- ----------------------------
-- Table structure for tb_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_resource`;
CREATE TABLE `tb_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '1' COMMENT '版本号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代码',
  `authority` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拦截值',
  `application_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '应用名称',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `sources` json NOT NULL COMMENT '所属来源',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父类 id',
  `sort` int NOT NULL DEFAULT 0 COMMENT '顺序值',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `page` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前端页面路径',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `category` tinyint NULL DEFAULT 20 COMMENT '类别',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_code`(`code` ASC) USING BTREE,
  INDEX `idx_resource_application_name`(`application_name` ASC) USING BTREE,
  INDEX `idx_resource_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 120 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_resource
-- ----------------------------
INSERT INTO `tb_resource` VALUES (1, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '开放平台商户客户端管理', 'open_platform_merchant_client', 'perms[auth_server_open_platform_merchant_client:get_by_merchant_id]', 'auth-server', 'security', '[\"CONSOLE\"]', NULL, 10, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (2, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', '3a90a80744ce7f6a91aca1aa30f37a44', 'perms[auth_server_open_platform_merchant_client:get]', 'auth-server', 'security', '[\"CONSOLE\"]', '1', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (3, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '保存或添加信息', '0a7c5b27401be3f3ada8185124a7a83d', 'perms[auth_server_open_platform_merchant_client:save]', 'auth-server', 'security', '[\"CONSOLE\"]', '1', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (4, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '基础配置', 'config', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 7, 'loncra-sliders-horizontal', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (5, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '字典管理', 'dictionary', '', 'resource-server', 'menu', '[\"CONSOLE\"]', '4', 0, 'loncra-book-text', '/resource-server/dictionary', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (6, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '资源管理', 'resource', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 6, 'loncra-package', '', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (7, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '机构管理', 'organization', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 5, 'loncra-network', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (8, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '员工管理', 'console_user', 'perms[auth_server_console_user:page]', 'auth-server', 'menu', '[\"CONSOLE\"]', '7', 0, 'loncra-user', '/auth-server/user/console', '', 10, 1);
INSERT INTO `tb_resource` VALUES (9, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', 'c046475dc248ddc7af98c6b7f08288a0', 'perms[auth_server_console_user:get]', 'auth-server', 'security', '[\"CONSOLE\"]', '8', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (10, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '删除信息', 'afd4b07821816b8aa644f32347fe8806', 'perms[auth_server_console_user:delete]', 'auth-server', 'security', '[\"CONSOLE\"]', '8', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (11, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '添加或保存信息', '665c404b040712df2bde9549400622ab', 'perms[auth_server_console_user:save]', 'auth-server', 'security', '[\"CONSOLE\"]', '8', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (12, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '权限管理', 'authority', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 3, 'loncra-shield', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (13, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '管理员重置用户密码', '164e8e4bb4bdc2c57e98956260b68fc3', 'perms[auth_server_system_user:admin_reset_password]', 'auth-server', 'security', '[\"CONSOLE\"]', '12', 2, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (14, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '修改个人登录密码', '39f6b6d9d45ffd681607a553f15422d8', '', 'auth-server', 'security', '[\"CONSOLE\"]', '12', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (15, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '角色管理', 'role', 'perms[auth_server_role:find]', 'auth-server', 'menu', '[\"CONSOLE\"]', '12', 3, 'loncra-users-round', '/auth-server/role', '', 10, 1);
INSERT INTO `tb_resource` VALUES (16, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', '78b58a8a8ff652401bbdf805573562d0', 'perms[auth_server_role:get]', 'auth-server', 'security', '[\"CONSOLE\"]', '15', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (17, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '删除信息', '469e530c2c2b7ed6594ad3f06453c954', 'perms[auth_server_role:delete]', 'auth-server', 'security', '[\"CONSOLE\"]', '15', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (18, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '添加或保存信息', 'ae181d9b4ecd9f80abf73d83225ace42', 'perms[auth_server_role:save]', 'auth-server', 'security', '[\"CONSOLE\"]', '15', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (19, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '资源管理', 'authority_resource', 'perms[auth_server_authority_resource:find]', 'auth-server', 'menu', '[\"CONSOLE\"]', '12', 0, 'loncra-shield-cog-corner', '/auth-server/resource', '', 10, 1);
INSERT INTO `tb_resource` VALUES (21, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', 'e3267da107a880c49ef4081e6c413bf2', 'perms[auth_server_authority_resource:get]', 'auth-server', 'security', '[\"CONSOLE\"]', '19', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (22, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '日志查询', 'log', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 9, 'loncra-logs', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (24, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '操作日志查询', 'operation_log', 'perms[auth_server_audit_event:operation_data_trace]', 'auth-server', 'menu', '[\"CONSOLE\"]', '22', 0, 'loncra-user-pen', '/auth-server/audit/event/operationDataTrace', '', 10, 1);
INSERT INTO `tb_resource` VALUES (25, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '登录日志查询', 'login_log', 'perms[auth_server_audit_event:authentication]', 'auth-server', 'menu', '[\"CONSOLE\"]', '22', 0, 'loncra-user-round-key', '/auth-server/audit/event/authentication', '', 10, 1);
INSERT INTO `tb_resource` VALUES (26, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '工作台', 'workbench', NULL, 'commons', 'tool', '[\"CONSOLE\"]', NULL, 1, 'loncra-presentation', '/commons/workbench', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (27, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '系统设置', 'setting', '', 'commons', 'profile', '[\"CONSOLE\"]', NULL, 8, 'loncra-settings', '/commons/setting', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (28, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '数据字典管理', 'data_dictionary', 'perms[resource_server_data_dictionary:page]', 'resource-server', 'security', '[\"CONSOLE\"]', '5', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (29, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', '1459e33c20574ee6dac6f6b6aaddb328', 'perms[resource_server_data_dictionary:get]', 'resource-server', 'security', '[\"CONSOLE\"]', '28', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (30, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '删除数据字典实体', '32892dbe9a2b70885465b607af26b873', 'perms[resource_server_data_dictionary:delete]', 'resource-server', 'security', '[\"CONSOLE\"]', '28', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (31, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '添加或保存数据字典', '1c0c2f7de6d6449e42fe0869f76d19b2', 'perms[resource_server_data_dictionary:save]', 'resource-server', 'security', '[\"CONSOLE\"]', '28', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (32, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '字典类型管理', 'dictionary_type', 'perms[resource_server_dictionary_type:find]', 'resource-server', 'security', '[\"CONSOLE\"]', '5', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (33, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '查看明细', '7ef8f908272fefbfcbdce08295fb682c', 'perms[resource_server_dictionary_type:get]', 'resource-server', 'security', '[\"CONSOLE\"]', '32', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (34, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '删除字典类型', 'b4479d979655b483b93a662acaea6314', 'perms[resource_server_dictionary_type:delete]', 'resource-server', 'security', '[\"CONSOLE\"]', '32', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (35, '2026-05-06 09:28:46', '2.0.0-SNAPSHOT', '添加或保存字典类型', 'f061c51b294f2f923cd55eead2344e9d', 'perms[resource_server_dictionary_type:save]', 'resource-server', 'security', '[\"CONSOLE\"]', '32', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (36, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '轮播图管理', 'carousel', 'perms[resource_server_carousel:page]', 'resource-server', 'menu', '[\"CONSOLE\"]', '4', 0, 'loncra-images', '/resource-server/carousel', '', 10, 1);
INSERT INTO `tb_resource` VALUES (37, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '下架信息', '77ac0acf03448565538832a5e3f53097', 'perms[resource_server_carousel:revoke]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (38, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '查看明细', 'f5b7ed1df893aa973fd1c3268a6e35c3', 'perms[resource_server_carousel:get]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (39, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '删除信息', 'c7e1b9e8ef540bff657a3a3ab0b1301e', 'perms[resource_server_carousel:delete]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (40, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '保存或添加信息', '89808678f047252e68534fb5c8ce9a4d', 'perms[resource_server_carousel:save]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (43, '2026-05-06 09:28:47', '2.0.0-SNAPSHOT', '删除信息', '09dcfade705e9864cb11dfee31ae42e9', '', 'resource-server', 'security', '[\"CONSOLE\", \"PERSONAL\"]', '88', 0, NULL, NULL, '', 10, 1);
INSERT INTO `tb_resource` VALUES (46, '2026-05-06 17:10:11', '2.0.0-SNAPSHOT', '消息管理', 'message', NULL, 'commons', 'root', '[\"CONSOLE\"]', NULL, 4, 'loncra-bell', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (47, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '邮件消息', 'email', 'perms[message_server_email:page]', 'message-server', 'menu', '[\"CONSOLE\"]', '46', 3, 'loncra-mail', '/message-server/email', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (48, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '查看明细', '5bbb420b12a090d214605e0fcc97b558', 'perms[message_server_email:get]', 'message-server', 'security', '[\"CONSOLE\"]', '47', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (49, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '删除信息', '3993845e9778003fd5813cff1247e0c5', 'perms[message_server_email:delete]', 'message-server', 'security', '[\"CONSOLE\"]', '47', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (50, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '发送信息', '0cd35aac3e5f2a60d265304f53c7628a', 'perms[message_server_email:send]', 'message-server', 'security', '[\"CONSOLE\"]', '47', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (51, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '短信消息', 'sms', 'perms[message_server_sms:page]', 'message-server', 'menu', '[\"CONSOLE\"]', '46', 1, 'loncra-phone', '/message-server/sms', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (56, '2026-05-06 17:10:12', '2.0.0-SNAPSHOT', '签名管理', 'sms_sign', 'perms[message_server_sms_sign:page]', 'message-server', 'navigationData', '[\"CONSOLE\"]', '51', 0, 'loncra-signature', '/message-server/sms/sign', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (61, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '模版管理', 'sms_template', 'perms[message_server_sms_template:page]', 'message-server', 'navigationData', '[\"CONSOLE\"]', '51', 0, 'loncra-layout-template', '/message-server/sms/template', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (64, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '批量消息', 'batch', 'perms[message_server_batch_message:page]', 'message-server', 'menu', '[\"CONSOLE\"]', '46', 2, 'loncra-replace', '/message-server/batch', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (65, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '查看明细', 'f1b3d8f578f14b75d37a3de3e43a4e19', 'perms[message_server_batch_message:get]', 'message-server', 'security', '[\"CONSOLE\"]', '64', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (66, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '删除信息', '6c31a792ac1f0436e150d2f9166902b2', 'perms[message_server_batch_message:delete]', 'message-server', 'security', '[\"CONSOLE\"]', '64', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (67, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '站内信消息', 'site', 'perms[message_server_site:page]', 'message-server', 'menu', '[\"CONSOLE\"]', '46', 0, 'loncra-message-square-code', '/message-server/site', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (68, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '查看明细', '77e00316a74d216bd1b2e919c685e8f1', 'perms[message_server_site:get]', 'message-server', 'security', '[\"CONSOLE\"]', '67', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (69, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '删除信息', '9465e16f15bf167a0d3b1c5ca99c5d56', 'perms[message_server_site:delete]', 'message-server', 'security', '[\"CONSOLE\"]', '67', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (70, '2026-05-06 17:10:13', '2.0.0-SNAPSHOT', '发送站内信', 'f87f11f2d18a2ed35b09fe8195d290ad', 'perms[message_server_site:send]', 'message-server', 'security', '[\"CONSOLE\"]', '67', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (71, '2026-05-11 15:21:58', '2.0.0-SNAPSHOT', '删除信息', '8f07400621fd3de7505173fd56f3609b', 'perms[auth_server_authority_resource:delete]', 'auth-server', 'security', '[\"CONSOLE\"]', '19', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (72, '2026-05-11 15:21:58', '2.0.0-SNAPSHOT', '添加或保存信息', '9019350f7ab52a5d38e5e45f17bd91ee', 'perms[auth_server_authority_resource:save]', 'auth-server', 'security', '[\"CONSOLE\"]', '19', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (73, '2026-05-11 15:21:58', '2.0.0-SNAPSHOT', '同步插件资源', '61a8d198c3f9eaec69d4314452f693a6', 'perms[auth_server_authority_resource:sync_plugin_resource]', 'auth-server', 'security', '[\"CONSOLE\"]', '19', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (76, '2026-05-15 12:10:29', '2.0.0-SNAPSHOT', '查看明细', '4fc55c9a905d4ae334758ad9b66b0b22', 'perms[auth_server_audit_event:get]', 'auth-server', 'security', '[\"CONSOLE\"]', '22', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (78, '2026-05-17 11:59:05', '2.0.0-SNAPSHOT', '排序', '553ac837f14db468f2b23fe8d294c855', 'perms[auth_server_authority_resource:sort]', 'auth-server', 'security', '[\"CONSOLE\"]', '19', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (79, '2026-05-17 11:59:05', '2.0.0-SNAPSHOT', '排序', 'b2613875bb666df905da1bd56f5996e0', 'perms[resource_server_data_dictionary:sort]', 'resource-server', 'security', '[\"CONSOLE\"]', '28', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (81, '2026-05-17 13:21:05', '2.0.0-SNAPSHOT', '导出查询结果', 'c5b75fabf00174bf0a30108fb40b43f4', 'perms[auth_server_console_user:export]', 'auth-server', 'security', '[\"CONSOLE\"]', '8', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (84, '2026-05-20 08:50:23', '2.0.0-SNAPSHOT', '排序', '60ceb5c483a49b2ef19b714c4f4f3cc3', 'perms[resource_server_data_dictionary:sort]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (85, '2026-05-20 09:36:38', '2.0.0-SNAPSHOT', '发布信息', '92fe24401cb50910fbe95d76b7244f63', 'perms[resource_server_carousel:release]', 'resource-server', 'security', '[\"CONSOLE\"]', '36', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (86, '2026-05-23 19:09:58', '2.0.0-SNAPSHOT', '更换个人头像', 'f6131133e9201877a64863dac3c16381', '', 'auth-server', 'security', '[\"CONSOLE\"]', '12', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (87, '2026-05-23 19:10:02', '2.0.0-SNAPSHOT', '修改个人头像', '82c8d2f6c41d9e451ea46bf85357c995', '', 'auth-server', 'security', '[\"CONSOLE\"]', '12', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (88, '2026-05-25 21:25:12', '2.0.0-SNAPSHOT', '我的资源', 'my_resource', 'isAuthenticated', 'resource-server', 'menu', '[\"CONSOLE\", \"PERSONAL\"]', '6', 0, 'loncra-user-star', '/resource-server/my/resource', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (89, '2026-05-25 21:25:12', '2.0.0-SNAPSHOT', '文件管理', 'attachment_manager', 'perms[resource_server_attachment:find]', 'resource-server', 'menu', '[\"CONSOLE\"]', '6', 0, 'loncra-cloud-upload', '/resource-server/file/manager', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (90, '2026-05-25 21:25:12', '2.0.0-SNAPSHOT', '删除文件', 'dc002f3f8f5010e7b0430f5c8fb345cc', 'perms[resource_server_attachment:delete]', 'resource-server', 'security', '[\"CONSOLE\"]', '89', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (91, '2026-05-26 10:14:37', '2.0.0-SNAPSHOT', '审计日志查询', 'audit_log', 'perms[auth_server_audit_event:audit]', 'auth-server', 'menu', '[\"CONSOLE\"]', '22', 0, 'loncra-user-search', NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (92, '2026-05-26 12:31:10', '2.0.0-SNAPSHOT', '导出数据', 'user_export', 'isAuthenticated', 'resource-server', 'tool', '[\"CONSOLE\"]', NULL, 0, 'loncra-file-down', '/commons/user/export', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (93, '2026-05-26 12:31:10', '2.0.0-SNAPSHOT', '删除信息', '5b4bcdb276105da0f3e112732b090ec3', 'isAuthenticated', 'resource-server', 'security', '[\"CONSOLE\"]', '92', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (103, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '查看明细', 'e98979bca1b54983871c8e82b1cb65e8', 'perms[message_server_sms_sign:get]', 'message-server', 'security', '[\"CONSOLE\"]', '56', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (106, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '发送短信', 'b477c3d6fc1d3a682647348339416ebd', 'perms[message_server_sms:send]', 'message-server', 'security', '[\"CONSOLE\"]', '51', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (107, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '查看短信余额', '76b50474e61adfddaf69ea9c5d9b4481', 'perms[message_server_sms:balance]', 'message-server', 'security', '[\"CONSOLE\"]', '51', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (108, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '查看明细', '285167c0634879996a4d2547c9f1a961', 'perms[message_server_sms:get]', 'message-server', 'security', '[\"CONSOLE\"]', '51', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (109, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '删除信息', '7383ff339620cebda0ac9aa297a4672a', 'perms[message_server_sms:delete]', 'message-server', 'security', '[\"CONSOLE\"]', '51', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (111, '2026-05-27 14:36:24', '2.0.0-SNAPSHOT', '查看明细', '866efd58df1774b317da7c66551fe84f', 'perms[message_server_sms_template:get]', 'message-server', 'security', '[\"CONSOLE\"]', '61', 0, NULL, NULL, NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (114, '2026-06-02 14:24:18', '2.0.0-SNAPSHOT', '我的消息', 'my_message', NULL, 'commons', 'tool', '[\"CONSOLE\"]', NULL, 1, 'loncra-message-circle', '/commons/my/message', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (115, '2026-06-03 18:10:27', '2.0.0-SNAPSHOT', '我的站内信', 'my_site_message', 'isAuthenticated', 'message-server', 'navigationData', '[\"CONSOLE\"]', '114', 0, 'loncra-message-square-text', '/commons/my/message/site', NULL, 10, 1);
INSERT INTO `tb_resource` VALUES (117, '2026-06-05 23:39:51', '2.0.0-SNAPSHOT', '我的聊天', 'my_chat_message', 'isFullyAuthenticated()', 'message-server', 'navigationData', '[\"CONSOLE\"]', '114', 0, 'loncra-messages-square', '/commons/my/message/chat', NULL, 10, 1);

-- ----------------------------
-- Table structure for tb_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_role`;
CREATE TABLE `tb_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `authority` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'spring security role 的 authority 值',
  `sources` json NOT NULL COMMENT '来源',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父类 id',
  `removable` tinyint NOT NULL COMMENT '是否可删除:0.否、1.是',
  `modifiable` tinyint NOT NULL COMMENT '是否可修改:0.否、1.是',
  `remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `resource_ids` json NULL COMMENT '资源信息',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_role
-- ----------------------------
INSERT INTO `tb_role` VALUES (1, '2026-05-06 16:36:13.890', 1, '超高级管理员', 'ADMIN', '[\"CONSOLE\"]', NULL, 0, 0, NULL, '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 43, 46, 47, 48, 49, 50, 51, 56, 61, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 76, 78, 79, 81, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 103, 106, 107, 108, 109, 111, 114, 115, 117, 119]', 1);

-- ----------------------------
-- Table structure for tb_site_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_site_message`;
CREATE TABLE `tb_site_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `channels` json NULL COMMENT '渠道名称',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容',
  `pushable` tinyint NOT NULL DEFAULT 1 COMMENT '是否推送消息：0.否，1.是',
  `readable` tinyint NOT NULL COMMENT '是否已读：0.否，1.是',
  `read_time` datetime(3) NULL DEFAULT NULL COMMENT '读取时间',
  `metadata` json NULL COMMENT '元数据信息',
  `retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常信息',
  `execute_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime(3) NULL DEFAULT NULL COMMENT '发送成功时间',
  `attachment_list` json NULL COMMENT '附件信息',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '批量消息 id',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `to_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收信人',
  `cover` json NULL COMMENT '封面',
  `retry_time` datetime NULL DEFAULT NULL COMMENT '重试事件',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '站内信消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_site_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_sms_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_sms_message`;
CREATE TABLE `tb_sms_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
  `channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '渠道名称',
  `phone_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `content` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容',
  `metadata` json NULL COMMENT '附加元数据信息',
  `retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` tinyint NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `execute_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0.执行中、1.执行成功，99.执行失败',
  `success_time` datetime NULL DEFAULT NULL COMMENT '成功时间',
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常信息',
  `batch_id` bigint NULL DEFAULT NULL COMMENT '批量消息 id',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `retry_time` datetime NULL DEFAULT NULL COMMENT '重试时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_phone_number`(`phone_number` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '短信消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_sms_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_call
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_call`;
CREATE TABLE `tb_user_chat_call`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `user_chat_room_id` bigint NOT NULL COMMENT '业务  id',
  `type` tinyint NOT NULL COMMENT '房间类型',
  `metadata` json NULL COMMENT '元数据信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL COMMENT '状态',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `scene` tinyint NULL DEFAULT NULL COMMENT '场景',
  `user_chat_message_id` bigint NULL DEFAULT NULL COMMENT '对应消息 id',
  `media_server` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '媒体服务器',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_user_chat_room_id`(`user_chat_room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天通话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_call
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_call_participant
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_call_participant`;
CREATE TABLE `tb_user_chat_call_participant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `user_chat_call_id` bigint NOT NULL COMMENT '聊天通话逐渐 id',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参与者',
  `type` tinyint NOT NULL COMMENT '类型',
  `metadata` json NULL COMMENT '元数据信息',
  `status` tinyint NOT NULL COMMENT '状态',
  `join_time` datetime NULL DEFAULT NULL COMMENT '加入时间',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '离开时间',
  `reconnect_time` datetime NULL DEFAULT NULL COMMENT '重连时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_user_chat_call_id`(`user_chat_call_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天房间参与者' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_call_participant
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_conversation
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_conversation`;
CREATE TABLE `tb_user_chat_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属用户',
  `user_chat_room_id` bigint NOT NULL COMMENT '房间 id',
  `pinned` tinyint NOT NULL COMMENT '是否置顶',
  `muted` tinyint NOT NULL COMMENT '是否免打扰',
  `last_user_chat_message_id` bigint NULL DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后收到消息时间',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话名称',
  `cover` json NULL COMMENT '封面',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `mentions` json NULL COMMENT '提及内容',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_principal_user_chat_room_id`(`principal` ASC, `user_chat_room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户聊天会话记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_conversation
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_message`;
CREATE TABLE `tb_user_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `user_chat_room_id` bigint NOT NULL COMMENT '聊天房间 id',
  `content` json NOT NULL COMMENT '内容',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送者',
  `undo` tinyint NOT NULL DEFAULT 0 COMMENT '是否撤销',
  `undo_time` datetime NULL DEFAULT NULL COMMENT '撤销时间',
  `type` tinyint NOT NULL DEFAULT 10 COMMENT '消息类型:10.用户消息, 20.系统消息',
  `undoable_time` datetime NULL DEFAULT NULL COMMENT '可撤销时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_user_chat_room_id`(`user_chat_room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天房间消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_message_read
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_message_read`;
CREATE TABLE `tb_user_chat_message_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `user_chat_message_id` bigint NOT NULL COMMENT '业务  id',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发送者',
  `readable` tinyint NOT NULL COMMENT '是否可读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '读取时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_chat_message_id`(`user_chat_message_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天消息已读列表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_message_read
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_participant
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_participant`;
CREATE TABLE `tb_user_chat_participant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `user_chat_room_id` bigint NOT NULL COMMENT '业务  id',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '参与者',
  `type` tinyint NOT NULL COMMENT '类型',
  `metadata` json NULL COMMENT '元数据信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `ix_user_chat_room_id`(`user_chat_room_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天房间参与者' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_participant
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user_chat_room
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_chat_room`;
CREATE TABLE `tb_user_chat_room`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `business_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务  id',
  `type` tinyint NOT NULL COMMENT '房间类型',
  `metadata` json NULL COMMENT '元数据信息',
  `business_scene` tinyint NOT NULL COMMENT '业务场景',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天房间' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user_chat_room
-- ----------------------------

-- ----------------------------
-- Table structure for tb_wechat_authentication
-- ----------------------------
DROP TABLE IF EXISTS `tb_wechat_authentication`;
CREATE TABLE `tb_wechat_authentication`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '更新版本号',
  `principal` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '认证用户',
  `session_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'session key',
  `open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'open id',
  `union_Id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'union id',
  `metadata` json NULL COMMENT '其他元数据信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '第三方认证信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_wechat_authentication
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
