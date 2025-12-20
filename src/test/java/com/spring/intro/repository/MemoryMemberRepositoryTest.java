package com.spring.intro.repository;

import com.spring.intro.domain.Member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


class MemoryMemberRepositoryTest {

    MemberRepository memberRepository = new MemoryMemberRepository();

    @AfterEach
    public void afterEach() {
        ((MemoryMemberRepository)memberRepository).clearStore();
    }

    @Test
    void save() {
        Member member = new Member();
        member.setName("hdh");

        memberRepository.save(member);
        Member result = memberRepository.findById(member.getId()).get();

        assertThat(member).isEqualTo(result);


    }

    @Test
    void findById() {
    }

    @Test
    void findAll() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        memberRepository.save(member1);
        Member member2 = new Member();
        member2.setName("spring2");
        memberRepository.save(member2);
        //when
        List<Member> result = memberRepository.findAll();
        //then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void findByName() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");
        memberRepository.save(member1);
        Member member2 = new Member();
        member2.setName("spring2");
        memberRepository.save(member2);
        //when
        Member result = memberRepository.findByName("spring1").get();
        //then
        assertThat(result).isEqualTo(member1);
    }

    @Test
    void clearStore() {
    }
}