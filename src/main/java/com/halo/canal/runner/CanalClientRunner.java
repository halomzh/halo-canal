package com.halo.canal.runner;

import com.halo.canal.client.HaloCanalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author shoufeng
 */

@Component
public class CanalClientRunner implements CommandLineRunner {

	@Autowired
	private HaloCanalClient haloCanalClient;

	@Override
	public void run(String... args) throws Exception {
		haloCanalClient.start();
	}

}
