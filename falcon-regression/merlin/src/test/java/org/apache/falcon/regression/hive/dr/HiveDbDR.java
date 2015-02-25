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

package org.apache.falcon.regression.hive.dr;

import org.apache.falcon.cli.FalconCLI;
import org.apache.falcon.entity.v0.EntityType;
import org.apache.falcon.entity.v0.Frequency;
import org.apache.falcon.regression.Entities.ClusterMerlin;
import org.apache.falcon.regression.Entities.RecipeMerlin;
import org.apache.falcon.regression.core.bundle.Bundle;
import org.apache.falcon.regression.core.helpers.ColoHelper;
import org.apache.falcon.regression.core.supportClasses.NotifyingAssert;
import org.apache.falcon.regression.core.util.BundleUtil;
import org.apache.falcon.regression.core.util.HiveAssert;
import org.apache.falcon.regression.core.util.InstanceUtil;
import org.apache.falcon.regression.core.util.TimeUtil;
import org.apache.falcon.regression.testHelper.BaseTestClass;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hive.hcatalog.api.HCatClient;
import org.apache.log4j.Logger;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.OozieClient;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.apache.falcon.regression.core.util.HiveUtil.runSql;
import static org.apache.falcon.regression.hive.dr.HiveObjectCreator.bootstrapCopy;
import static org.apache.falcon.regression.hive.dr.HiveObjectCreator.createVanillaTable;

/**
 * Hive DR Testing for Hive database replication.
 */
@Test(groups = "embedded")
public class HiveDbDR extends BaseTestClass {
    private static final Logger LOGGER = Logger.getLogger(HiveDbDR.class);
    private final ColoHelper cluster = servers.get(0);
    private final ColoHelper cluster2 = servers.get(1);
    private final FileSystem clusterFS = serverFS.get(0);
    private final FileSystem clusterFS2 = serverFS.get(1);
    private final OozieClient clusterOC = serverOC.get(0);
    private HCatClient clusterHC;
    private HCatClient clusterHC2;
    RecipeMerlin recipeMerlin;
    Connection connection;
    Connection connection2;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        clusterHC = cluster.getClusterHelper().getHCatClient();
        clusterHC2 = cluster2.getClusterHelper().getHCatClient();
        bundles[0] = BundleUtil.readHCatBundle();
        bundles[0] = new Bundle(bundles[0], cluster);
        bundles[1] = new Bundle(bundles[0], cluster2);
        bundles[0].generateUniqueBundle();
        bundles[1].generateUniqueBundle();
        final ClusterMerlin srcCluster = bundles[0].getClusterElement();
        final ClusterMerlin tgtCluster = bundles[1].getClusterElement();
        Bundle.submitCluster(bundles[0]);

        recipeMerlin = RecipeMerlin.readFromDir("HiveDrRecipe",
            FalconCLI.RecipeOperation.HIVE_DISASTER_RECOVERY)
            .withRecipeCluster(srcCluster);
        recipeMerlin.withSourceCluster(srcCluster)
            .withTargetCluster(tgtCluster)
            .withFrequency(new Frequency("5", Frequency.TimeUnit.minutes))
            .withValidity(TimeUtil.getTimeWrtSystemTime(-1), TimeUtil.getTimeWrtSystemTime(6));
        recipeMerlin.setUniqueName(this.getClass().getSimpleName());

        connection = cluster.getClusterHelper().getHiveJdbcConnection();

        connection2 = cluster2.getClusterHelper().getHiveJdbcConnection();
    }

    private void setUpDb(String dbName, Connection conn) throws SQLException {
        runSql(conn, "drop database if exists " + dbName + " cascade");
        runSql(conn, "create database " + dbName);
        runSql(conn, "use " + dbName);
    }

    @Test
    public void drDbDropDb() throws Exception {
        final String dbName = "drDbDropDb";
        setUpDb(dbName, connection);
        setUpDb(dbName, connection2);
        recipeMerlin.withSourceDb(dbName).withSourceTable("*");
        final List<String> command = recipeMerlin.getSubmissionCommand();

        final long srcReplId = clusterHC.getCurrentNotificationEventId();
        runSql(connection2, "alter database " + dbName + " set dbproperties " +
            "(\"repl.last.id\"=\"" + srcReplId + "\")");

        Assert.assertEquals(Bundle.runFalconCLI(command), 0, "Recipe submission failed.");

        runSql(connection, "drop database " + dbName);

        InstanceUtil.waitTillInstanceReachState(clusterOC, recipeMerlin.getName(), 1,
            CoordinatorAction.Status.SUCCEEDED, EntityType.PROCESS);

        final List<String> dstDbs = runSql(connection2, "show databases");
        Assert.assertFalse(dstDbs.contains(dbName), "dstDbs = " + dstDbs + " was not expected to " +
            "contain " + dbName);
    }

    @Test
    public void drDbAddDropTable() throws Exception {
        final String dbName = "drDbAddDropTable";
        final String tblToBeDropped = "table_to_be_dropped";
        final String tblToBeDroppedAndAdded = "table_to_be_dropped_and_readded";
        final String newTableToBeAdded = "new_table_to_be_added";

        setUpDb(dbName, connection);
        setUpDb(dbName, connection2);
        recipeMerlin.withSourceDb(dbName).withSourceTable("*")
            .withFrequency(new Frequency("2", Frequency.TimeUnit.minutes));
        final List<String> command = recipeMerlin.getSubmissionCommand();

        createVanillaTable(connection, tblToBeDropped);
        createVanillaTable(connection, tblToBeDroppedAndAdded);
        bootstrapCopy(connection, clusterFS, tblToBeDropped,
            connection2, clusterFS2, tblToBeDropped);
        bootstrapCopy(connection, clusterFS, tblToBeDroppedAndAdded,
            connection2, clusterFS2, tblToBeDroppedAndAdded);
        final long srcReplId = clusterHC.getCurrentNotificationEventId();
        runSql(connection2, "alter database " + dbName + " set dbproperties " +
            "(\"repl.last.id\"=\"" + srcReplId + "\")");

        /* For first replication - two tables are dropped & one table is added */
        runSql(connection, "drop table " + tblToBeDropped);
        runSql(connection, "drop table " + tblToBeDroppedAndAdded);
        createVanillaTable(connection, newTableToBeAdded);

        Assert.assertEquals(Bundle.runFalconCLI(command), 0, "Recipe submission failed.");

        InstanceUtil.waitTillInstanceReachState(clusterOC, recipeMerlin.getName(), 1,
            CoordinatorAction.Status.SUCCEEDED, EntityType.PROCESS);

        HiveAssert.assertDbEqual(cluster, clusterHC.getDatabase(dbName),
            cluster2, clusterHC2.getDatabase(dbName), new NotifyingAssert(true)).assertAll();

        /* For second replication - a dropped tables is added back */
        createVanillaTable(connection, tblToBeDroppedAndAdded);

        InstanceUtil.waitTillInstanceReachState(clusterOC, recipeMerlin.getName(), 2,
            CoordinatorAction.Status.SUCCEEDED, EntityType.PROCESS);

        HiveAssert.assertDbEqual(cluster, clusterHC.getDatabase(dbName),
            cluster2, clusterHC2.getDatabase(dbName), new NotifyingAssert(true)).assertAll();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws IOException {
        try {
            prism.getProcessHelper().deleteByName(recipeMerlin.getName(), null);
        } catch (Exception e) {
            LOGGER.info("Deletion of process: " + recipeMerlin.getName() + " failed with " +
                "exception: " +e);
        }
        removeBundles();
        cleanTestDirs();
    }
}
