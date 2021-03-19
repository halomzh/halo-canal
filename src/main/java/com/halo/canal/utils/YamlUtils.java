package com.halo.canal.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * yaml工具
 *
 * @author shoufeng
 */

public class YamlUtils {

	public static <T> T generateObject(String sourcePath, Class<T> tClass) {
		Yaml yaml = new Yaml(new Constructor(tClass));

		return yaml.load(YamlUtils.class.getClassLoader().getResourceAsStream(sourcePath));
	}

}
