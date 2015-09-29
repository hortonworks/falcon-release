/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.falcon.ADFService.util;

/**
 * ADF JSON Constants in ADF request.
 */
public final class ADFJsonConstants {

    public static final String ADF_REQUEST_ACTIVITY = "activity";
    public static final String ADF_REQUEST_TRANSFORMATION = "transformation";
    public static final String ADF_REQUEST_TYPE = "type";
    public static final String ADF_REQUEST_JOBID = "jobId";
    public static final String ADF_REQUEST_START_TIME = "dataSliceStart";
    public static final String ADF_REQUEST_END_TIME = "dataSliceEnd";
    public static final String ADF_REQUEST_SCHEDULER = "scheduler";
    public static final String ADF_REQUEST_FREQUENCY = "frequency";
    public static final String ADF_REQUEST_INTERVAL = "interval";
    public static final String ADF_REQUEST_LINKED_SERVICES = "linkedServices";
    public static final String ADF_REQUEST_NAME = "name";
    public static final String ADF_REQUEST_INPUTS = "inputs";
    public static final String ADF_REQUEST_OUTPUTS = "outputs";
    public static final String ADF_REQUEST_TABLES = "tables";
    public static final String ADF_REQUEST_PROPERTIES = "properties";
    public static final String ADF_REQUEST_EXTENDED_PROPERTIES = "extendedProperties";
    public static final String ADF_REQUEST_CLUSTER_NAME = "clusterName";
    public static final String ADF_REQUEST_RUN_ON_BEHALF_USER = "runOnBehalf";
    public static final String ADF_REQUEST_LOCATION = "location";
    public static final String ADF_REQUEST_FOLDER_PATH = "folderPath";
    public static final String ADF_REQUEST_SCRIPT = "script";
    public static final String ADF_REQUEST_SCRIPT_PATH = "scriptPath";
    public static final String ADF_REQUEST_LINKED_SERVICE_NAME = "linkedServiceName";
    public static final String ADF_REQUEST_TABLE_NAME = "hivetable";
    public static final String ADF_REQUEST_TABLE_PARTITION = "partition";

    private ADFJsonConstants() {
    }
}
