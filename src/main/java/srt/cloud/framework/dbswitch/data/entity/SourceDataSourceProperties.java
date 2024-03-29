// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package srt.cloud.framework.dbswitch.data.entity;

import lombok.Data;
import srt.cloud.framework.dbswitch.common.entity.PatternMapper;
import srt.cloud.framework.dbswitch.common.type.DBTableType;
import srt.cloud.framework.dbswitch.common.type.ProductTypeEnum;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class SourceDataSourceProperties {


	private ProductTypeEnum sourceProductType;
	private String url;
	private String driverClassName;
	private String username;
	private String password;
	private Long connectionTimeout = TimeUnit.SECONDS.toMillis(60);
	private Long maxLifeTime = TimeUnit.MINUTES.toMillis(60);

	private Integer fetchSize = 5000;
	private DBTableType tableType;
	private String sourceSchema = "";
	private Integer includeOrExclude;
	private String sourceIncludes = "";
	private String sourceExcludes = "";
	private List<PatternMapper> regexTableMapper;
	private List<PatternMapper> regexColumnMapper;
}
