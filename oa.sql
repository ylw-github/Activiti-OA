

DROP TABLE IF EXISTS `leaveapply`;
CREATE TABLE `leaveapply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `process_instance_id` varchar(45) DEFAULT NULL,
  `user_id` varchar(20) DEFAULT NULL,
  `start_time` varchar(45) DEFAULT NULL,
  `end_time` varchar(45) DEFAULT NULL,
  `leave_type` varchar(45) DEFAULT NULL,
  `reason` varchar(400) DEFAULT NULL,
  `apply_time` varchar(100) DEFAULT NULL,
  `reality_start_time` varchar(45) DEFAULT NULL,
  `reality_end_time` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `permission`;
 CREATE TABLE `permission` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `permissionname` varchar(45) NOT NULL,
  PRIMARY KEY (`pid`),
  UNIQUE KEY `permissionname_UNIQUE` (`permissionname`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `rolename` varchar(45) NOT NULL,
  PRIMARY KEY (`rid`),
  UNIQUE KEY `rolename_UNIQUE` (`rolename`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `rpid` int(11) NOT NULL AUTO_INCREMENT,
  `roleid` int(11) NOT NULL,
  `permissionid` int(11) NOT NULL,
  PRIMARY KEY (`rpid`),
  KEY `a_idx` (`roleid`),
  KEY `b_idx` (`permissionid`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45) COLLATE utf8_bin NOT NULL,
  `password` varchar(45) COLLATE utf8_bin NOT NULL,
  `tel` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;




DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `urid` int(11) NOT NULL AUTO_INCREMENT,
  `userid` int(11) NOT NULL,
  `roleid` int(11) NOT NULL,
  PRIMARY KEY (`urid`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (27, '产品测试');
INSERT INTO `permission` VALUES (23, '人力审核');
INSERT INTO `permission` VALUES (24, '总经理审核');
INSERT INTO `permission` VALUES (25, '总裁办审核');
INSERT INTO `permission` VALUES (29, '撤回单处理');
INSERT INTO `permission` VALUES (26, '硬件开发');
INSERT INTO `permission` VALUES (20, '软件开发');
INSERT INTO `permission` VALUES (28, '销假处理');
INSERT INTO `permission` VALUES (22, '项目经理审核');
-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (17, 'Java软件工程师');
INSERT INTO `role` VALUES (23, '人力专员');
INSERT INTO `role` VALUES (24, '总裁');
INSERT INTO `role` VALUES (19, '测试工程师');
INSERT INTO `role` VALUES (22, '研发中心总经理');
INSERT INTO `role` VALUES (21, '项目经理');
-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (46, 19, 27);
INSERT INTO `role_permission` VALUES (50, 21, 27);
INSERT INTO `role_permission` VALUES (51, 21, 26);
INSERT INTO `role_permission` VALUES (52, 21, 20);
INSERT INTO `role_permission` VALUES (53, 21, 22);
INSERT INTO `role_permission` VALUES (54, 22, 24);
INSERT INTO `role_permission` VALUES (55, 22, 22);
INSERT INTO `role_permission` VALUES (56, 23, 23);
INSERT INTO `role_permission` VALUES (57, 24, 25);
INSERT INTO `role_permission` VALUES (60, 17, 29);
INSERT INTO `role_permission` VALUES (61, 17, 20);
INSERT INTO `role_permission` VALUES (62, 17, 28);
-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (36, '索隆', '1234', '13100000000', 18);
INSERT INTO `user` VALUES (37, '香吉士', '1234', '18262015301', 18);
INSERT INTO `user` VALUES (38, '乔巴', '1234', '17925544455', 18);
INSERT INTO `user` VALUES (39, '路飞', '1234', '18265556325', 18);
INSERT INTO `user` VALUES (40, '红发', '1234', '18220004152', 30);
INSERT INTO `user` VALUES (41, '白胡子', '1234', '18802221148', 70);
INSERT INTO `user` VALUES (42, '罗杰', '1234', '18922002264', 88);
-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (93, 36, 17);
INSERT INTO `user_role` VALUES (94, 37, 17);
INSERT INTO `user_role` VALUES (95, 38, 19);
INSERT INTO `user_role` VALUES (96, 39, 21);
INSERT INTO `user_role` VALUES (97, 40, 22);
INSERT INTO `user_role` VALUES (98, 41, 23);
INSERT INTO `user_role` VALUES (99, 42, 24);