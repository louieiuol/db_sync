// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package srt.cloud.framework.dbswitch.dbcommon.database.impl;

import srt.cloud.framework.dbswitch.dbcommon.database.IDatabaseOperator;

import javax.sql.DataSource;

/**
 * MySQL数据库实现类
 *
 * @author jrl
 */
public class DorisDatabaseOperator extends MysqlDatabaseOperator implements IDatabaseOperator {

	public DorisDatabaseOperator(DataSource dataSource) {
		super(dataSource);
	}

}
