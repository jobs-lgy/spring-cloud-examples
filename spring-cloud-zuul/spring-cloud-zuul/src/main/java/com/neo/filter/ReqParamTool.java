package com.neo.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @Program fund-user-gateway
 * @Description param util
 * @Author LGY
 * @Date 2019-05-06
 */
public class ReqParamTool {
    public static Map<String,Object> getParamMap(HttpServletRequest request){

        Map<String, String[]> parameterMap  = request.getParameterMap();
        try {

            String contentType = request.getContentType();
            if(StrUtil.containsAnyIgnoreCase(contentType, ContentType.JSON.toString())){
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                if(sb.length() >0){
                    String json = sb.toString();
                    JSONObject jsonObject = JSON.parseObject(json);
                    Map<String, Object> jsonMap = ReqParamTool.recursion(jsonObject);
                    jsonMap.forEach((k,v)->parameterMap.put(k,new String[]{Convert.toStr(v)}));
                }
            }
            ReqParamTool.trimParam(parameterMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReqParamTool.map2Map(parameterMap);
    }

    private static Map<String, Object> map2Map(Map<String, String[]> parameterMap) {
        Map<String, Object> resultMap = new HashMap<>(parameterMap.size());
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
        for (Map.Entry<String, String[]> entry : entries) {
            resultMap.put(entry.getKey(),ArrayUtil.isEmpty(entry.getValue())?null:entry.getValue()[0]);
        }
        return resultMap;
    }

    /**
     * 参数处理
     * @param parameterMap
     */
    public static void trimParam(Map<String , String[]> parameterMap){
        if(MapUtil.isEmpty(parameterMap)){
            return;
        }
        parameterMap.forEach((k,v)->{
            if(ArrayUtil.isEmpty(v)){
                parameterMap.put(k, null);
                return;
            }
            for (int i = 0; i < v.length; i++) {
                v[i]= StrUtil.trimToNull(v[i]);
            }
            parameterMap.put(k, v);
        });
    }


    public static JSONObject recursion(JSONObject jsonObject){
        Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
        JSONObject map = new JSONObject();
        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if(value instanceof JSONObject){
                JSONObject jm = (JSONObject) value;
                map.putAll(recursion(key,jm));
                continue;
            }
            if(value instanceof JSONArray){
                JSONArray array = (JSONArray) value;
                Object[] objects = array.toArray();
                JSONObject amap = new JSONObject();
                for (int i = 0; i < objects.length; i++) {
                    amap.put(StrUtil.format("{}[{}]",key,i),objects[i]);
                }
                map.putAll(recursion(amap));
                continue;
            }
            map.put(key, value);
        }
        return map;
    }
    private static JSONObject recursion(String preKey,JSONObject jsonObject){
        Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
        JSONObject map = new JSONObject();
        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if(value instanceof JSONObject){
                JSONObject jm = (JSONObject) value;
                map.putAll(recursion(StrUtil.format("{}.{}",preKey,key),jm));
                continue;
            }
            if(value instanceof JSONArray){
                JSONArray array = (JSONArray) value;
                Object[] objects = array.toArray();
                JSONObject amap = new JSONObject();
                for (int i = 0; i < objects.length; i++) {
                    amap.put(StrUtil.format("{}[{}]",key,i),objects[i]);
                }
                map.putAll(recursion(preKey,amap));
                continue;
            }
            map.put(StrUtil.format("{}.{}",preKey,key), value);
        }
        return map;
    }
}
