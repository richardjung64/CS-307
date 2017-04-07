package com.styln.demo.nosql;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.util.ThreadUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.styln.R;
import com.amazonaws.models.nosql.UsersDO;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DemoNoSQLTableUsers extends DemoNoSQLTableBase {
    private static final String LOG_TAG = DemoNoSQLTableUsers.class.getSimpleName();

    /** Inner classes use this value to determine how many results to retrieve per service call. */
    private static final int RESULTS_PER_RESULT_GROUP = 40;

    /** Removing sample data removes the items in batches of the following size. */
    private static final int MAX_BATCH_SIZE_FOR_DELETE = 50;

    /********* Primary Get Query Inner Classes *********/

    public class DemoGetWithPartitionKey extends DemoNoSQLOperationBase {
        private UsersDO result;
        private boolean resultRetrieved = true;

        private DemoGetWithPartitionKey(final Context context) {
            super(context.getString(R.string.nosql_operation_get_by_partition_text),
                String.format(context.getString(R.string.nosql_operation_example_get_by_partition_text),
                    "userId", AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID()));
        }

        /* Blocks until result is retrieved, should be called in the background. */
        @Override
        public boolean executeOperation() throws AmazonClientException {
            // Retrieve an item by passing the partition key using the object mapper.
            result = mapper.load(UsersDO.class, AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());

            if (result != null) {
                resultRetrieved = false;
                return true;
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            if (resultRetrieved) {
                return null;
            }
            final List<DemoNoSQLResult> results = new ArrayList<>();
            results.add(new DemoNoSQLUsersResult(result));
            resultRetrieved = true;
            return results;
        }

        @Override
        public void resetResults() {
            resultRetrieved = false;
        }

    }

    /* ******** Secondary Named Index Query Inner Classes ******** */

    /********* Scan Inner Classes *********/

    public class DemoScanWithFilter extends DemoNoSQLOperationBase {

        private PaginatedScanList<UsersDO> results;
        private Iterator<UsersDO> resultsIterator;

        DemoScanWithFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_with_filter),
                String.format(context.getString(R.string.nosql_operation_example_scan_with_filter),
                    "User_Age", "1111500000"));
        }

        @Override
        public boolean executeOperation() {
            // Use an expression names Map to avoid the potential for attribute names
            // colliding with DynamoDB reserved words.
            final Map <String, String> filterExpressionAttributeNames = new HashMap<>();
            filterExpressionAttributeNames.put("#User_Age", "User_Age");

            final Map<String, AttributeValue> filterExpressionAttributeValues = new HashMap<>();
            filterExpressionAttributeValues.put(":MinUser_Age",
                new AttributeValue().withN("1111500000"));
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#User_Age > :MinUser_Age")
                .withExpressionAttributeNames(filterExpressionAttributeNames)
                .withExpressionAttributeValues(filterExpressionAttributeValues);

            results = mapper.scan(UsersDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    public class DemoScanWithoutFilter extends DemoNoSQLOperationBase {

        private PaginatedScanList<UsersDO> results;
        private Iterator<UsersDO> resultsIterator;

        DemoScanWithoutFilter(final Context context) {
            super(context.getString(R.string.nosql_operation_title_scan_without_filter),
                context.getString(R.string.nosql_operation_example_scan_without_filter));
        }

        @Override
        public boolean executeOperation() {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            results = mapper.scan(UsersDO.class, scanExpression);
            if (results != null) {
                resultsIterator = results.iterator();
                if (resultsIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<DemoNoSQLResult> getNextResultGroup() {
            return getNextResultsGroupFromIterator(resultsIterator);
        }

        @Override
        public boolean isScan() {
            return true;
        }

        @Override
        public void resetResults() {
            resultsIterator = results.iterator();
        }
    }

    /**
     * Helper Method to handle retrieving the next group of query results.
     * @param resultsIterator the iterator for all the results (makes a new service call for each result group).
     * @return the next list of results.
     */
    private static List<DemoNoSQLResult> getNextResultsGroupFromIterator(final Iterator<UsersDO> resultsIterator) {
        if (!resultsIterator.hasNext()) {
            return null;
        }
        List<DemoNoSQLResult> resultGroup = new LinkedList<>();
        int itemsRetrieved = 0;
        do {
            // Retrieve the item from the paginated results.
            final UsersDO item = resultsIterator.next();
            // Add the item to a group of results that will be displayed later.
            resultGroup.add(new DemoNoSQLUsersResult(item));
            itemsRetrieved++;
        } while ((itemsRetrieved < RESULTS_PER_RESULT_GROUP) && resultsIterator.hasNext());
        return resultGroup;
    }

    /** The DynamoDB object mapper for accessing DynamoDB. */
    private final DynamoDBMapper mapper;

    public DemoNoSQLTableUsers() {
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    }

    @Override
    public String getTableName() {
        return "Users";
    }

    @Override
    public String getPartitionKeyName() {
        return "Artist";
    }

    public String getPartitionKeyType() {
        return "String";
    }

    @Override
    public String getSortKeyName() {
        return null;
    }

    public String getSortKeyType() {
        return "";
    }

    @Override
    public int getNumIndexes() {
        return 0;
    }

    @Override
    public void insertSampleData() throws AmazonClientException {
        /*Log.d(LOG_TAG, "Inserting data.");
        final UsersDO firstItem = new UsersDO();

        //firstItem.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());
        firstItem.setUserAge(DemoSampleDataGenerator.getRandomSampleNumber());
        firstItem.setUserDescription(
                DemoSampleDataGenerator.getRandomSampleString("User_Description"));
        firstItem.setUserGender(
                DemoSampleDataGenerator.getRandomSampleString("User_Gender"));
        firstItem.setUserName(
                DemoSampleDataGenerator.getRandomSampleString("User_Name"));
        firstItem.setUserPhoto(
                DemoSampleDataGenerator.getRandomSampleString("User_Photo"));
        firstItem.setUserPosts(DemoSampleDataGenerator.getSampleList());
        firstItem.setUserPrivacy(DemoSampleDataGenerator.getRandomSampleBinary());
        firstItem.setUsersFollowers(DemoSampleDataGenerator.getSampleStringSet());
        firstItem.setUsersFollowing(DemoSampleDataGenerator.getSampleStringSet());
        AmazonClientException lastException = null;

        try {
            mapper.save(firstItem);
        } catch (final AmazonClientException ex) {
            Log.e(LOG_TAG, "Failed saving item : " + ex.getMessage(), ex);
            lastException = ex;
        }

//        final UsersDO[] items = new UsersDO[SAMPLE_DATA_ENTRIES_PER_INSERT-1];
//        for (int count = 0; count < SAMPLE_DATA_ENTRIES_PER_INSERT-1; count++) {
//            final UsersDO item = new UsersDO();
//            //item.setUserId(DemoSampleDataGenerator.getRandomSampleString("userId"));
//            item.setUserAge(DemoSampleDataGenerator.getRandomSampleNumber());
//            item.setUserGender(DemoSampleDataGenerator.getRandomSampleString("User_Gender"));
//            item.setUserPhoto(DemoSampleDataGenerator.getRandomSampleString("User_Photo"));
////            item.setUserPost(DemoSampleDataGenerator.getRandomSampleString("User_Post"));
////            item.setUserPrivacy(DemoSampleDataGenerator.getRandomSampleBinary());
////            item.setUsersFollowers(DemoSampleDataGenerator.getSampleList());
////            item.setUsersFollowing(DemoSampleDataGenerator.getSampleList());
//
//            items[count] = item;
//        }
//        try {
//            mapper.batchSave(Arrays.asList(items));
//        } catch (final AmazonClientException ex) {
//            Log.e(LOG_TAG, "Failed saving item batch : " + ex.getMessage(), ex);
//            lastException = ex;
//        }
//
//        if (lastException != null) {
//            // Re-throw the last exception encountered to alert the user.
//            throw lastException;
//        }*/
    }

    @Override
    public void removeSampleData() throws AmazonClientException {

        final UsersDO itemToFind = new UsersDO();
        //itemToFind.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());

        final DynamoDBQueryExpression<UsersDO> queryExpression = new DynamoDBQueryExpression<UsersDO>()
            .withHashKeyValues(itemToFind)
            .withConsistentRead(false)
            .withLimit(MAX_BATCH_SIZE_FOR_DELETE);

        final PaginatedQueryList<UsersDO> results = mapper.query(UsersDO.class, queryExpression);

        Iterator<UsersDO> resultsIterator = results.iterator();

        AmazonClientException lastException = null;

        if (resultsIterator.hasNext()) {
            final UsersDO item = resultsIterator.next();

            // Demonstrate deleting a single item.
            try {
                mapper.delete(item);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item : " + ex.getMessage(), ex);
                lastException = ex;
            }
        }

        final List<UsersDO> batchOfItems = new LinkedList<UsersDO>();
        while (resultsIterator.hasNext()) {
            // Build a batch of books to delete.
            for (int index = 0; index < MAX_BATCH_SIZE_FOR_DELETE && resultsIterator.hasNext(); index++) {
                batchOfItems.add(resultsIterator.next());
            }
            try {
                // Delete a batch of items.
                mapper.batchDelete(batchOfItems);
            } catch (final AmazonClientException ex) {
                Log.e(LOG_TAG, "Failed deleting item batch : " + ex.getMessage(), ex);
                lastException = ex;
            }

            // clear the list for re-use.
            batchOfItems.clear();
        }


        if (lastException != null) {
            // Re-throw the last exception encountered to alert the user.
            // The logs contain all the exceptions that occurred during attempted delete.
            throw lastException;
        }
    }

    private List<DemoNoSQLOperationListItem> getSupportedDemoOperations(final Context context) {
        List<DemoNoSQLOperationListItem> noSQLOperationsList = new ArrayList<DemoNoSQLOperationListItem>();
            noSQLOperationsList.add(new DemoGetWithPartitionKey(context));

        noSQLOperationsList.add(new DemoNoSQLOperationListHeader(
            context.getString(R.string.nosql_operation_header_scan)));
        noSQLOperationsList.add(new DemoScanWithoutFilter(context));
        noSQLOperationsList.add(new DemoScanWithFilter(context));
        return noSQLOperationsList;
    }

    @Override
    public void getSupportedDemoOperations(final Context context,
                                           final SupportedDemoOperationsHandler opsHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<DemoNoSQLOperationListItem> supportedOperations = getSupportedDemoOperations(context);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        opsHandler.onSupportedOperationsReceived(supportedOperations);
                    }
                });
            }
        }).start();
    }
}
