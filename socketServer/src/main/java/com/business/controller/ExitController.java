package com.business.controller;

import com.business.entry.SocketMessage;
import com.business.entry.User;
import com.result.base.annotation.On;
import com.result.base.annotation.Route;
import com.result.base.cache.Client;
import com.result.base.security.SecurityUtil;
import com.result.base.tools.ObjectUtil;
import com.result.base.tools.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 作者 huangxinyu
 * @version 创建时间：2018年1月16日 下午3:58:40 类说明
 */

@Route
public class ExitController {
	private static final Logger logger = LoggerFactory.getLogger(ExitController.class);

	@On("Disconnect")
	public void onDisconnect(Client client) {
		if (ObjectUtil.isNull(client))
			return;
		client.leaveNameSpace();
		if (!client.isJoinRoom())
			return;
		SocketMessage repBuild = new SocketMessage();
		repBuild.setClientUri("leaveRoom");
		User user = SecurityUtil.getLoginUser(client.getToken(), User.class);
		repBuild.setId(user.getUserId());
		repBuild.setParam1(user.getUserName());
		repBuild.setParam2(user.getUserImg());
		client.roomBroadcast(SerializationUtil.serializeToByte(repBuild));
		client.leaveRoom();
		logger.info("有人退出了");
	}
}
