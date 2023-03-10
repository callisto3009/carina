/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.dataprovider.core.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import com.qaprosoft.carina.core.foundation.dataprovider.annotations.XlsDataSourceParameters;
import com.qaprosoft.carina.core.foundation.dataprovider.core.groupping.GroupByMapper;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.DSBean;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSParser;
import com.qaprosoft.carina.core.foundation.dataprovider.parser.XLSTable;
import com.zebrunner.carina.utils.ParameterGenerator;

/**
 * Created by Patotsky on 16.12.2014.
 */
public class XlsDataProvider extends BaseDataProvider {

    @Override
    public Object[][] getDataProvider(Annotation annotation, ITestContext context, ITestNGMethod testMethod) {

        XlsDataSourceParameters parameters = (XlsDataSourceParameters) annotation;

        DSBean dsBean = new DSBean(parameters, context.getCurrentXmlTest().getAllParameters());

        XLSTable dsData = XLSParser.parseSpreadSheet(dsBean.getDsFile(), dsBean.getXlsSheet(), dsBean.getExecuteColumn(), dsBean.getExecuteValue());

        argsList = dsBean.getArgs();
        staticArgsList = dsBean.getStaticArgs();

        if (parameters.dsArgs().isEmpty()) {
            GroupByMapper.setIsHashMapped(true);
        }

        String groupByParameter = parameters.groupColumn();
        if (!groupByParameter.isEmpty()) {
            GroupByMapper.getInstanceInt().add(argsList.indexOf(groupByParameter));
            GroupByMapper.getInstanceStrings().add(groupByParameter);
        }

        String testRailColumn = "";
        if (!parameters.testRailColumn().isEmpty())
            testRailColumn = parameters.testRailColumn();

        if (!parameters.qTestColumn().isEmpty() && testRailColumn.isEmpty())
            testRailColumn = parameters.qTestColumn();

        String testMethodColumn = "";
        if (!parameters.testMethodColumn().isEmpty())
            testMethodColumn = parameters.testMethodColumn();

        String testMethodOwnerColumn = "";
        if (!parameters.testMethodOwnerColumn().isEmpty())
            testMethodOwnerColumn = parameters.testMethodOwnerColumn();

        String bugColumn = "";
        if (!parameters.bugColumn().isEmpty())
            bugColumn = parameters.bugColumn();

        int width = 0;
        if (argsList.size() == 0) {
            width = staticArgsList.size() + 1;
        } else {
            width = argsList.size() + staticArgsList.size();
        }
        Object[][] args = new Object[dsData.getDataRows().size()][width];

        int rowIndex = 0;
        for (Map<String, String> xlsRow : dsData.getDataRows()) {
            String testName = context.getName();

            if (argsList.size() == 0) {
                // process each column in xlsRow data obligatory replacing special keywords like UUID etc
                for (Map.Entry<String, String> entry : xlsRow.entrySet()) {
                    if (entry == null)
                        continue;

                    String value = entry.getValue();
                    if (value == null)
                        continue;

                    Object param = ParameterGenerator.process(entry.getValue().toString());
                    if (param == null)
                        continue;

                    String newValue = param.toString();
                    if (!value.equals(newValue)) {
                        entry.setValue(newValue);
                    }
                }
                args[rowIndex][0] = xlsRow;
                for (int i = 0; i < staticArgsList.size(); i++) {
                    args[rowIndex][i + 1] = getStaticParam(staticArgsList.get(i), context, dsBean);
                }
            } else {
                int i;
                for (i = 0; i < argsList.size(); i++) {
                    args[rowIndex][i] = ParameterGenerator.process(xlsRow
                            .get(argsList.get(i)));
                }
                // populate the rest of items by static parameters from testParams
                for (int j = 0; j < staticArgsList.size(); j++) {
                    args[rowIndex][i + j] = getStaticParam(staticArgsList.get(j), context, dsBean);
                }
            }

            tuidMap.put(hash(args[rowIndex], testMethod), getValueFromRow(xlsRow, dsBean.getUidArgs()));

            if (!testMethodColumn.isEmpty()) {
                String testNameOverride = getValueFromRow(xlsRow, List.of(testMethodColumn));
                if (!testNameOverride.isEmpty()) {
                    testColumnNamesMap.put(hash(args[rowIndex], testMethod), getValueFromRow(xlsRow, testMethodColumn));
                }
            }

            // add testMethoOwner from xls datasource to special hashMap
            addValueToSpecialMap(testMethodOwnerArgsMap, testMethodOwnerColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);

            // add testrails cases from xls datasource to special hashMap
            addValueToSpecialMap(testRailsArgsMap, testRailColumn, String.valueOf(Arrays.hashCode(args[rowIndex])), xlsRow);

            rowIndex++;
        }

        return args;
    }

    private String getValueFromRow(Map<String, String> xlsRow, String key) {
        return getValueFromRow(xlsRow, List.of(key));
    }

    private String getValueFromRow(Map<String, String> xlsRow, List<String> keys) {
        StringBuilder valueRes = new StringBuilder();

        for (String key : keys) {
            if (xlsRow.containsKey(key)) {
                String value = xlsRow.get(key);
                if (value != null && !value.isEmpty()) {
                    valueRes.append(value);
                    valueRes.append(", ");
                }
            }
        }

        if (valueRes.indexOf(",") != -1) {
            valueRes.replace(valueRes.length() - 2, valueRes.length() - 1, "");
        }
        return valueRes.toString();
    }
}
