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

package org.apache.falcon.ADFService;

import org.apache.falcon.ADFService.util.FSUtils;
import org.apache.falcon.FalconException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Class for process.
 */
public class Process {
    private static final String PROCESS_TEMPLATE_FILE = "process.xml";

    private String entityName;
    private String frequency;
    private String startTime;
    private String endTime;
    private String clusterName;
    private String inputName;
    private String inputFeedName;
    private String outputName;
    private String outputFeedName;
    private String engineType;
    private String wfPath;
    private String aclOwner;
    private String properties;

    public Process(final Builder builder) {
        this.entityName = builder.name;
        this.clusterName = builder.processClusterName;
        this.frequency = builder.processFrequency;
        this.startTime = builder.processStartTime;
        this.endTime = builder.processEndTime;
        this.inputName = builder.processInputName;
        this.inputFeedName = builder.processInputFeedName;
        this.outputName = builder.processOutputName;
        this.outputFeedName = builder.processOutputFeedName;
        this.engineType = builder.processEngineType;
        this.wfPath = builder.processWfPath;
        this.aclOwner = builder.processAclOwner;
        this.properties = builder.processProperties;
    }

    public String getName() {
        return entityName;
    }

    public String getEntityxml() throws FalconException {
        try {
            String template = FSUtils.readTemplateFile(ADFJob.HDFS_URL_PORT,
                    ADFJob.TEMPLATE_PATH_PREFIX + PROCESS_TEMPLATE_FILE);
            return template.replace("$processName$", entityName)
                    .replace("$frequency$", frequency)
                    .replace("$startTime$", startTime)
                    .replace("$endTime$", endTime)
                    .replace("$clusterName$", clusterName)
                    .replace("$input$", inputName)
                    .replace("$inputFeedName$", inputFeedName)
                    .replace("$output$", outputName)
                    .replace("$outputFeedName$", outputFeedName)
                    .replace("$engine$", engineType)
                    .replace("$scriptPath$", wfPath)
                    .replace("$aclowner$", aclOwner)
                    .replace("$properties$", properties);
        } catch (IOException e) {
            throw new FalconException("Error when generating entity xml for table feed", e);
        } catch (URISyntaxException e) {
            throw new FalconException("Error when generating entity xml for table feed", e);
        }
    }

    /**
     * Builder for process.
     */
    public static class Builder {
        private String name;
        private String processClusterName;
        private String processFrequency;
        private String processStartTime;
        private String processEndTime;
        private String processInputName;
        private String processInputFeedName;
        private String processOutputName;
        private String processOutputFeedName;
        private String processEngineType;
        private String processWfPath;
        private String processAclOwner;
        private String processProperties;

        public Process build() {
            return new Process(this);
        }

        public Builder withProcessName(final String processName) {
            this.name = processName;
            return this;
        }

        public Builder withClusterName(final String clusterName) {
            this.processClusterName = clusterName;
            return this;
        }

        public Builder withFrequency(final String frequency) {
            this.processFrequency = frequency;
            return this;
        }

        public Builder withStartTime(final String startTime) {
            this.processStartTime = startTime;
            return this;
        }

        public Builder withEndTime(final String endTime) {
            this.processEndTime = endTime;
            return this;
        }

        public Builder withInputName(final String inputName) {
            this.processInputName = inputName;
            return this;
        }

        public Builder withInputFeedName(final String inputFeedName) {
            this.processInputFeedName = inputFeedName;
            return this;
        }

        public Builder withOutputName(final String outputName) {
            this.processOutputName = outputName;
            return this;
        }

        public Builder withOutputFeedName(final String outputFeedName) {
            this.processOutputFeedName = outputFeedName;
            return this;
        }

        public Builder withAclOwner(final String aclOwner) {
            this.processAclOwner = aclOwner;
            return this;
        }

        public Builder withEngineType(final String engineType) {
            this.processEngineType = engineType;
            return this;
        }

        public Builder withWFPath(final String wfPath) {
            this.processWfPath = wfPath;
            return this;
        }

        public Builder withProperties(final String properties) {
            this.processProperties = properties;
            return this;
        }
    }

}
