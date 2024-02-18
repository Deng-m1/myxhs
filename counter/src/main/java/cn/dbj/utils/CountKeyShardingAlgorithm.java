package cn.dbj.utils;

import cn.dbj.framework.starter.common.Constant.RedisKeyConstant;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.stereotype.Component;

import java.util.*;


public class CountKeyShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {


    @Override
    public void init(Properties props) {
        map.put(RedisKeyConstant.ATTENTIONS_NUMBER,0);
        map.put(RedisKeyConstant.FOLLOWS_NUMBER,1);
        map.put(RedisKeyConstant.POST_LIKE,2);
        map.put(RedisKeyConstant.COMMENT_LIKE,3);
        map.put(RedisKeyConstant.COMMENT_NUMBER,4);
    }

    private static Map<String,Integer> map=new HashMap<>();

    private Collection<String> getShardingValue(ComplexKeysShardingValue<String> shardingValues, final String key) {
        Collection<String> valueSet = new ArrayList<>();
        Map<String, Collection<String>> columnNameAndShardingValuesMap = shardingValues.getColumnNameAndShardingValuesMap();
        if (columnNameAndShardingValuesMap.containsKey(key)) {
            valueSet.addAll(columnNameAndShardingValuesMap.get(key));
        }
        return valueSet;
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, ComplexKeysShardingValue<String> complexKeysShardingValue) {
        // 得到每个分片健对应的值
        Collection<String> countKey = this.getShardingValue(complexKeysShardingValue, "count_key");
        Collection<String> objId = this.getShardingValue(complexKeysShardingValue, "obj_id");

        String s = countKey.stream().findFirst().get();
        String s1 = objId.stream().findFirst().get();
        Integer q=(map.get(s)*4+s1.hashCode()%4);
        String suffix = q.toString();

        Collection<String> result = new LinkedHashSet<>(collection.size());
        result.add(complexKeysShardingValue.getLogicTableName()+"_"+suffix);
        // 对两个分片健同时取模的方式分库
       /* for (String ck : countKey) {
            for (String id : objId) {
                Integer q=(map.get(ck)*4+id.hashCode()%4);
                String suffix = q.toString();
                for (String databaseName : collection) {
                    if (databaseName.endsWith(suffix)) {
                        shardingSuffix.add(databaseName);
                    }
                }
            }
        }*/
        return result;
    }
}
