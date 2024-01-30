package com.virnect.platform.workspace.global.util;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import com.virnect.platform.workspace.global.constant.UUIDType;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class RandomStringTokenUtil {
	public static String generate(UUIDType type, int digit) {
		switch (type) {
			case WORKSPACE_UUID: {
				return UUID.randomUUID().toString().replaceAll("-", "");
			}
			case PIN_NUMBER: {
				Random random = new Random(System.currentTimeMillis());

				int range = (int)Math.pow(10, 6);
				int trim = (int)Math.pow(10, 5);
				int result = random.nextInt(range) + trim;

				if (result > range) {
					result = result - trim;
				}

				return String.valueOf(result);
			}
			case INVITE_CODE: {
				return RandomStringUtils.randomAlphanumeric(20);
			}
		}
		return UUID.randomUUID().toString();
	}
}
