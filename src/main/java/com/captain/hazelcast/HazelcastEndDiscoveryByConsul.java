package com.captain.hazelcast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class HazelcastEndDiscoveryByConsul {
	
	private static Properties initSysProperties(){
		System.setProperty("address.ip", "10.100.100.43");
		System.setProperty("address.port", String.valueOf(5001));
		
		System.setProperty("consulHost", "10.100.102.151");
		System.setProperty("consulPort", String.valueOf(8500));
		System.setProperty("consulAclToken", "");
		System.setProperty("consulSslEnabled", String.valueOf(false));
		System.setProperty("consulSslServerCertFilePath","");
		System.setProperty("consulSslServerCertBase64","");
		System.setProperty("consulSslServerHostnameVerify", String.valueOf(false));
		System.setProperty("consulHealthCheckProvider", "org.bitsofinfo.hazelcast.discovery.consul.TcpHealthCheckBuilder");
		
		return System.getProperties();
	}
	
	public static HazelcastInstance instanceOfEndPoint() {
		
		Config config = new ClasspathXmlConfig("hazelcast-consul-discovery-spi-example.xml", initSysProperties());
		
		HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
		
		instance.getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				Member eventMember = membershipEvent.getMember();
				System.out.println("member remove " + eventMember.getAddress());
			}

			@Override
			public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
				System.out.println("member change " + memberAttributeEvent.getMember());
			}

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
				Member eventMember = membershipEvent.getMember();
				System.out.println("member add " + eventMember.getAddress());
			}
		});

		return instance;
	}

	private static void getMaster(HazelcastInstance ins1) {
		Iterator<Member> iter = ins1.getCluster().getMembers().iterator();
		if (iter.hasNext()) {
			Member instance = iter.next();
			System.out.println("Master is " + instance.toString());
		}
	}

	public static void main(String[] args) throws InterruptedException {
		HazelcastInstance ins1 = instanceOfEndPoint();
		int port = 5001;
		
		System.out.println("----------- nodeId : " + port);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = new String();
			while (!input.equals("quit")) {
				System.out.print(">");
				input = reader.readLine();
				if (input.equals("quit")) {
					break;
				} else if (input.equals("showMembers")) {
					System.out.println(ins1.getCluster().getMembers());
				} else if (input.equals("getMaster")) {
					getMaster(ins1);
				} else if (input.equals("shutdown")) {
					ins1.shutdown();
					reader.close();
				}else {
					System.out.println("You input " + input);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
