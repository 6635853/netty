package com.csh.network.util;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

/**
 * JBoss Marshalling 序列化
 * 
 * @author Administrator
 * @date 2019年5月13日
 * @package com.csh.network.util
 */
public class SerializableFactoryMarshalling {

	/**
	 * 创建Jboss Marshalling解码器MarshallingDecoder
	 * 
	 * @return
	 */
	public static MarshallingDecoder buildMarshallingDecoder() {
		// 首先通过Marshalling工具类的精通方法获取Marshalling实例对象，参数serial标识创建的是java序列化工厂对象
		final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
		// 创建MarshallingConfiguration对象，版本号为5
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		// 序列化版本号，只要使用jdk5以上的版本，version只能定义为5
		configuration.setVersion(5);
		// 根据MarshallerFactory和Configuration创建provider
		UnmarshallerProvider provider = new DefaultUnmarshallerProvider(marshallerFactory, configuration);
		// 构建Netty的MarshallingDecoder 对象，两个参数分别是provider和单个消息序列化的最大长度
		MarshallingDecoder decoder = new MarshallingDecoder(provider, 1024 * 1024 * 1);
		return decoder;
	}

	/**
	 * 创建Jboss Marshalling编码器MarshallingEncoder
	 * 
	 * @return
	 */
	public static MarshallingEncoder buildMarshallingEncoder() {
		final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		MarshallerProvider provider = new DefaultMarshallerProvider(marshallerFactory, configuration);
		// 构建netty的MarshallingEncoderd对象，MarshallingEncoder用于实现序列化接口的pojo对象
		MarshallingEncoder encoder = new MarshallingEncoder(provider);
		return encoder;
	}
}
