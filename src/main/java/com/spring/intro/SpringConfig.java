package com.spring.intro;

import com.spring.intro.repository.MemberRepository;
import com.spring.intro.repository.MemoryMemberRepository;
import com.spring.intro.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

}

