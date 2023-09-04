// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package srt.cloud.framework.dbswitch.core.util;

import srt.cloud.framework.dbswitch.common.constant.Const;
import srt.cloud.framework.dbswitch.common.type.ProductTypeEnum;
import srt.cloud.framework.dbswitch.core.database.AbstractDatabase;
import srt.cloud.framework.dbswitch.core.database.DatabaseFactory;
import srt.cloud.framework.dbswitch.core.model.ColumnDescription;
import srt.cloud.framework.dbswitch.core.model.ColumnMetaData;
import srt.cloud.framework.dbswitch.core.model.TableDescription;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼接SQL工具类
 *
 * @author jrl
 */
public final class GenerateSqlUtils {

	public static String getDDLCreateTableSQL(
			ProductTypeEnum type,
			List<ColumnDescription> fieldNames,
			List<String> primaryKeys,
			String schemaName,
			String tableName,
			boolean autoIncr) {
		AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);
		return getDDLCreateTableSQL(
				db,
				fieldNames,
				primaryKeys,
				schemaName,
				tableName,
				false,
				null,
				autoIncr);
	}

	public static String getDDLCreateTableSQL(
			AbstractDatabase db,
			List<ColumnDescription> fieldNames,
			List<String> primaryKeys,
			String schemaName,
			String tableName,
			boolean withRemarks,
			String tableRemarks,
			boolean autoIncr) {
		ProductTypeEnum type = db.getDatabaseType();
		StringBuilder sb = new StringBuilder();
		List<String> pks = fieldNames.stream()
				.filter((cd) -> primaryKeys.contains(cd.getFieldName()))
				.map(ColumnDescription::getFieldName)
				.collect(Collectors.toList());

		sb.append(Const.CREATE_TABLE);
		// if(ifNotExist && type!=DatabaseType.ORACLE) {
		// sb.append( Const.IF_NOT_EXISTS );
		// }
		sb.append(db.getQuotedSchemaTableCombination(schemaName, tableName));
		sb.append("(");

		for (int i = 0; i < fieldNames.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			} else {
				sb.append("  ");
			}

			ColumnMetaData v = fieldNames.get(i).getMetaData();
			sb.append(db.getFieldDefinition(v, pks, autoIncr, false, withRemarks));
		}

		if (!pks.isEmpty() && !ProductTypeEnum.DORIS.equals(type)) {
			String pk = db.getPrimaryKeyAsString(pks);
			sb.append(", PRIMARY KEY (").append(pk).append(")");
		}

		sb.append(")");
		if (ProductTypeEnum.MYSQL.equals(type)) {
			sb.append("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin");
			if (withRemarks && StringUtils.isNotBlank(tableRemarks)) {
				sb.append(String.format(" COMMENT='%s' ", tableRemarks.replace("'", "\\'")));
			}
		} else if (ProductTypeEnum.DORIS.equals(type)) {
			if (!pks.isEmpty()) {
				String pk = db.getPrimaryKeyAsString(pks);
				sb.append("unique key(").append(pk).append(")").append(Const.CR);
			}
			if (withRemarks && StringUtils.isNotBlank(tableRemarks)) {
				sb.append(String.format(" COMMENT '%s' ", tableRemarks.replace("'", "\\'")));
				sb.append(Const.CR);
			}
			sb.append(String.format("DISTRIBUTED BY HASH(%s) BUCKETS 10", !pks.isEmpty() ? pks.get(0) : fieldNames.get(0).getFieldName())).append(Const.CR).append("PROPERTIES(\"replication_num\" = \"1\");");
		}

		return DDLFormatterUtils.format(sb.toString());
	}

	public static List<String> getDDLCreateTableSQL(
			ProductTypeEnum type,
			List<ColumnDescription> fieldNames,
			List<String> primaryKeys,
			String schemaName,
			String tableName,
			String tableRemarks,
			boolean autoIncr) {
		AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);
		List<String> results = new ArrayList<>(2);
		String createTableSql = getDDLCreateTableSQL(db, fieldNames, primaryKeys, schemaName,
				tableName, true, tableRemarks, autoIncr);
		results.add(createTableSql);
		if (type.noCommentStatement()) {
			return results;
		}

		TableDescription td = new TableDescription();
		td.setSchemaName(schemaName);
		td.setTableName(tableName);
		td.setRemarks(tableRemarks);
		td.setTableType("TABLE");
		results = db.getTableColumnCommentDefinition(td, fieldNames);
		results.add(0, createTableSql);
		return results;
	}

}
