package com.atguigu.gmall;

import com.atguigu.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

	@Autowired
	RedisUtil redisUtil;


	public static void main(String[] args) {

	}

	public String  a (){
		System.out.println("a");

		b();

		return "a";//b();
	}

	public String  b (){
		System.out.println("a");

		return "b";
	}


	@Test
	public void contextLoads() {

		Jedis jedis = redisUtil.getJedis();

		String ping = jedis.ping();

		System.out.println(ping);
	}

}
