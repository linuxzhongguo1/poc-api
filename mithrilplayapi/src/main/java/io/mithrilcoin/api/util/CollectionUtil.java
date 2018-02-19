package io.mithrilcoin.api.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Component;

/***
 * 
 * @author Kei
 *
 */
@Component
public class CollectionUtil {

	/**
	 * String 필드명으로 Key를 만들고 내부에 리스트를 채워주는 함수.
	 * 
	 * @param list
	 * @param keyfieldName
	 * @return
	 * @throws Exception
	 */
	public <T> HashMap<String, ArrayList<T>> list2MapArrayValue(ArrayList<T> list, String keyfieldName) {
		HashMap<String, ArrayList<T>> resultMap = new HashMap<>();
		try {

			for (T data : list) {
				Field keyField = data.getClass().getDeclaredField(keyfieldName);
				keyField.setAccessible(true);
				Object extractValue = keyField.get(data);
				String key = String.valueOf(extractValue);
				if (!resultMap.containsKey(extractValue)) {
					ArrayList<T> subList = new ArrayList<>();
					subList.add(data);
					resultMap.put(key, subList);
				} else {
					resultMap.get(key).add(data);
				}
			}
		} catch (Exception e) {
		}
		return resultMap;
	}

	/**
	 * String 필드명으로 Key를 잡은 후 Map의 value에 할당하는 함수. 같은 키가 입력될 경우 후열의 값으로 overwrite.
	 * 
	 * @param list
	 * @param keyfieldName
	 * @return
	 * @throws Exception
	 */
	public <T> HashMap<String, T> list2MapSingleValue(ArrayList<T> list, String keyfieldName) {
		HashMap<String, T> resultMap = new HashMap<>();
		try {

			for (T data : list) {
				Field keyField = data.getClass().getDeclaredField(keyfieldName);
				keyField.setAccessible(true);
				Object extractValue = keyField.get(data);
				String key = String.valueOf(extractValue);
				resultMap.put(key, data);
			}
		} catch (Exception e) {

		}
		return resultMap;
	}
}
