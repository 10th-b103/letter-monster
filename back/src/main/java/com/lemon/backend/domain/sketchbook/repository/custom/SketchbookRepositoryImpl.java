package com.lemon.backend.domain.sketchbook.repository.custom;

import com.lemon.backend.domain.characters.dto.CharacterMotionToSketchbookDto;
import com.lemon.backend.domain.letter.dto.requestDto.LetterToSketchbookDto;
import com.lemon.backend.domain.sketchbook.dto.responseDto.*;
import com.lemon.backend.domain.sketchbook.entity.Sketchbook;
import com.lemon.backend.domain.sketchbook.entity.SketchbookCharacterMotion;
import com.lemon.backend.domain.users.user.dto.response.UserGetDto;
import com.lemon.backend.global.exception.CustomException;
import com.lemon.backend.global.exception.ErrorCode;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.lemon.backend.domain.characters.entity.QCharacterMotion.characterMotion;
import static com.lemon.backend.domain.characters.entity.QCharacters.characters;
import static com.lemon.backend.domain.letter.entity.QLetter.letter;
import static com.lemon.backend.domain.sketchbook.entity.QSketchbook.sketchbook;
import static com.lemon.backend.domain.sketchbook.entity.QSketchbookCharacterMotion.sketchbookCharacterMotion;
import static com.querydsl.core.types.Projections.constructor;
import static com.querydsl.jpa.JPAExpressions.select;

@RequiredArgsConstructor
public class SketchbookRepositoryImpl implements SketchbookRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public Optional<List<SketchbookGetSimpleDto>> getSketchList(Integer userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }

        List<SketchbookGetSimpleDto> sketchDtos = query
                .select(constructor(SketchbookGetSimpleDto.class,
                        sketchbook.id,
                        sketchbook.isPublic,
                        sketchbook.shareLink,
                        sketchbook.name,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag),
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        sketchbook.users.id.eq(userId).as("isWritePossible"),
                        sketchbook.isRepresent
                )).from(sketchbook)
                .where(sketchbook.users.id.eq(userId))
                .fetch();

        System.out.println(userId);

        return Optional.ofNullable(sketchDtos.isEmpty() ? null : sketchDtos);
    }

    @Override
    public Optional<List<SketchbookGetSimpleDto>> getFriendSketchList(Integer userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }
        List<SketchbookGetSimpleDto> sketchDtos = query
                .select(constructor(SketchbookGetSimpleDto.class,
                        sketchbook.id,
                        sketchbook.isPublic,
                        sketchbook.shareLink,
                        sketchbook.name,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag),
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        sketchbook.users.id.eq(userId).as("isWritePossible"),
                        sketchbook.isRepresent
                )).from(sketchbook)
                .where(sketchbook.users.id.eq(userId).and(sketchbook.isPublic.eq(true)))
                .fetch();
        return Optional.ofNullable(sketchDtos.isEmpty() ? null : sketchDtos);
    }


//    @Override
//    public Optional<SketchbookGetDto> getSketchSelect(String sketchId) {
//        if (sketchId == null) {
//            throw new CustomException(ErrorCode.INVALID_ACCESS);
//        }
//        SketchbookGetDto sketchDto = query
//                .select(constructor(SketchbookGetDto.class,
//                        sketchbook.id,
//                        sketchbook.isPublic,
//                        sketchbook.shareLink,
//                        sketchbook.name,
//                        sketchbook.isWritePossible,
//                        Projections.fields(UserGetDto.class,
//                                sketchbook.users.nickname,
//                                sketchbook.users.nicknameTag),
//                        sketchbook.sketchbookUuid,
//                        sketchbook.tag
//                )).from(sketchbook)
//                .where(sketchbook.sketchbookUuid.eq(sketchId))
//                .fetchOne();
//        return Optional.ofNullable(sketchDto);
//    }

    @Override
    public Optional<SketchbookGetDetailDto> getSketchSelect(String sketchId) {
        if (sketchId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }

        // Sketchbook 상세 정보 조회
        SketchbookGetDetailDto sketchDto = query
                .select(Projections.constructor(SketchbookGetDetailDto.class,
                        sketchbook.id,
                        sketchbook.isPublic,
                        sketchbook.shareLink,
                        sketchbook.name,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag),
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        sketchbook.isWritePossible,
                        sketchbook.isRepresent))
                .from(sketchbook)
                .where(sketchbook.sketchbookUuid.eq(sketchId))
                .fetchOne();

        if (sketchDto != null) {
            // SketchbookCharacterMotion과 관련된 데이터를 조인하여 한 번에 가져오기
            List<SketchbookCharacterMotionGetListDto> sketchbookCharacterMotionGetListDtos = query
                    .select(Projections.constructor(SketchbookCharacterMotionGetListDto.class,
                                    sketchbookCharacterMotion.id,
                                    Projections.constructor(CharacterMotionToSketchbookDto.class,
                                            characterMotion.id,
                                            characterMotion.motion.id,
                                            characterMotion.url,
                                            characters.nickname),
                                    Projections.list(
                                            Projections.constructor(LetterToSketchbookDto.class,
                                                    letter.id,
                                                    Projections.fields(UserGetDto.class,
                                                            letter.sender.nickname,
                                                            letter.sender.nicknameTag),
                                                    Projections.fields(UserGetDto.class,
                                                            letter.receiver.nickname,
                                                            letter.receiver.nicknameTag),
                                                    letter.content,
                                                    letter.isPublic,
                                                    letter.createdAt)
                                    )
                            )
                    )
                    .from(sketchbookCharacterMotion)
                    .leftJoin(sketchbookCharacterMotion.characterMotion, characterMotion)
                    .leftJoin(characterMotion.characters, characters)
                    .leftJoin(letter).on(letter.sketchbookCharacterMotion.id.eq(sketchbookCharacterMotion.id))
                    .where(sketchbookCharacterMotion.sketchbook.sketchbookUuid.eq(sketchId))
                    .fetch();

            sketchDto.setSketchbookCharacterMotionList(sketchbookCharacterMotionGetListDtos);
        }

        return Optional.ofNullable(sketchDto);
    }


    @Override
    public Optional<SketchbookCharacterMotion> findByCharacterMotionAndSketchbook(Long sketchbookId, Long characterMotionId) {
        if (sketchbookId == null || characterMotionId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }
        SketchbookCharacterMotion SketchbookCharacterMotion = query
                .select(constructor(SketchbookCharacterMotion.class,
                        sketchbookCharacterMotion.id,
                        sketchbookCharacterMotion.sketchbook,
                        sketchbookCharacterMotion.characterMotion))
                .from(sketchbookCharacterMotion)
                .where(sketchbookCharacterMotion.sketchbook.id.eq(sketchbookId).and(sketchbookCharacterMotion.characterMotion.id.eq(characterMotionId)))
                .fetchOne();
        return Optional.ofNullable(SketchbookCharacterMotion);
    }

    @Override
    public Optional<SketchbookGetDetailDto> getSketchSelect2(Integer userId, String sketchId) {
        if (sketchId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }

        // Sketchbook 상세 정보 조회
        SketchbookGetDetailDto sketchDto = query
                .select(Projections.constructor(SketchbookGetDetailDto.class,
                        sketchbook.id,
                        sketchbook.isPublic,
                        sketchbook.shareLink,
                        sketchbook.name,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag),
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        sketchbook.users.id.eq(userId).as("isWritePossible"),
                        sketchbook.isRepresent)
                )
                .from(sketchbook)
                .where(sketchbook.sketchbookUuid.eq(sketchId))
                .fetchOne();

        if (sketchDto != null) {
            // SketchbookCharacterMotion과 관련된 Letter를 조인하여 한 번에 가져오기
            List<SketchbookCharacterMotionGetListDto> sketchbookCharacterMotions = query
                    .select(Projections.constructor(SketchbookCharacterMotionGetListDto.class,
                                    sketchbookCharacterMotion.id,
                                    Projections.constructor(CharacterMotionToSketchbookDto.class,
                                            characterMotion.id,
                                            characterMotion.motion.id,
                                            characterMotion.url,
                                            characters.nickname),
                                    Projections.list(
                                            Projections.constructor(LetterToSketchbookDto.class,
                                                    letter.id,
                                                    Projections.fields(UserGetDto.class,
                                                            letter.sender.nickname,
                                                            letter.sender.nicknameTag),
                                                    Projections.fields(UserGetDto.class,
                                                            letter.receiver.nickname,
                                                            letter.receiver.nicknameTag),
                                                    letter.content,
                                                    letter.isPublic,
                                                    letter.createdAt)
                                    )
                            )
                    )
                    .from(sketchbookCharacterMotion)
                    .leftJoin(sketchbookCharacterMotion.characterMotion, characterMotion)
                    .leftJoin(characterMotion.characters, characters)
                    .leftJoin(letter).on(letter.sketchbookCharacterMotion.id.eq(sketchbookCharacterMotion.id))
                    .where(sketchbookCharacterMotion.sketchbook.sketchbookUuid.eq(sketchId))
                    .fetch();

            sketchDto.setSketchbookCharacterMotionList(sketchbookCharacterMotions);
        }

        return Optional.ofNullable(sketchDto);
    }


    @Override
    public SketchbookDetailPageDto getSketchSelect3(String sketchId, Pageable pageable) {
        if (sketchId == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS);
        }

        // Sketchbook 상세 정보 조회
        SketchbookGetDetailDto sketchDto = query
                .select(Projections.constructor(SketchbookGetDetailDto.class,
                        sketchbook.id,
                        sketchbook.isPublic,
                        sketchbook.shareLink,
                        sketchbook.name,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag),
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        sketchbook.isWritePossible,
                        sketchbook.isRepresent))
                .from(sketchbook)
                .where(sketchbook.sketchbookUuid.eq(sketchId))
                .fetchOne();

        if(sketchDto == null) {
            throw new CustomException(ErrorCode.SKETCHBOOK_NOT_FOUND);
        }
        // SketchbookCharacterMotion과 관련된 Letter를 조인하여 한 번에 가져오기
        QueryResults<SketchbookCharacterMotionGetListDto> results = query
                .select(Projections.constructor(SketchbookCharacterMotionGetListDto.class,
                                sketchbookCharacterMotion.id,
                                Projections.constructor(CharacterMotionToSketchbookDto.class,
                                        characterMotion.id,
                                        characterMotion.motion.id,
                                        characterMotion.url,
                                        characters.nickname),
                                Projections.list(
                                        Projections.constructor(LetterToSketchbookDto.class,
                                                letter.id,
                                                Projections.fields(UserGetDto.class,
                                                        letter.sender.nickname,
                                                        letter.sender.nicknameTag),
                                                Projections.fields(UserGetDto.class,
                                                        letter.receiver.nickname,
                                                        letter.receiver.nicknameTag),
                                                letter.content,
                                                letter.isPublic,
                                                letter.createdAt)
                                )
                        )
                )
                .from(sketchbookCharacterMotion)
                .leftJoin(sketchbookCharacterMotion.characterMotion, characterMotion)
                .leftJoin(characterMotion.characters, characters)
                .leftJoin(letter).on(letter.sketchbookCharacterMotion.id.eq(sketchbookCharacterMotion.id))
                .where(sketchbookCharacterMotion.sketchbook.sketchbookUuid.eq(sketchId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<SketchbookCharacterMotionGetListDto> motions = results.getResults();
        long total = results.getTotal();

        Page<SketchbookCharacterMotionGetListDto> page = new PageImpl<>(motions, pageable, total);
        return new SketchbookDetailPageDto(sketchDto, page);
    }



    @Override
    public Optional<String> findHighestSketchbookTagByName(String name) {
        String highestSketchbookTag = query.select(sketchbook.tag)
                .from(sketchbook)
                .where(sketchbook.name.eq(name))
                .orderBy(sketchbook.tag.desc())
                .fetchFirst();
        return Optional.ofNullable(highestSketchbookTag);
    }

    @Override
    public Optional<List<SketchbookGetAllDto>> getSketchAll() {
        List<SketchbookGetAllDto> sketchAll = query
                .select(constructor(SketchbookGetAllDto.class,
                        sketchbook.id,
                        sketchbook.name,
                        sketchbook.tag)).from(sketchbook)
                .fetch();
        return Optional.ofNullable(sketchAll.isEmpty() ? null : sketchAll);
    }

    @Override
    public Optional<List<SketchbookSearchGetDto>> searchList(String sketchbookName) {

        if (sketchbookName == null || sketchbookName.trim().isEmpty()) {
            return Optional.empty(); // 또는 기본 목록 반환
        }
        
        List<SketchbookSearchGetDto> list = query
                .select(Projections.constructor(SketchbookSearchGetDto.class,
                        sketchbook.id,
                        sketchbook.sketchbookUuid,
                        sketchbook.name,
                        sketchbook.tag,
                        sketchbook.users.nickname,
                        sketchbook.isPublic))
                .from(sketchbook)
                .leftJoin(sketchbook.users)
                .where(sketchbook.name.contains(sketchbookName).and(sketchbook.isPublic.eq(true)))
                .fetch();
        return Optional.ofNullable(list);
    }

    @Override
    public SketchbookGetRandomDto randomSketchbook(){
        return query
                .select(Projections.constructor(SketchbookGetRandomDto.class,
                        sketchbook.id,
                        sketchbook.shareLink,
                        sketchbook.name,
                        sketchbook.sketchbookUuid,
                        sketchbook.tag,
                        Projections.fields(UserGetDto.class,
                                sketchbook.users.nickname,
                                sketchbook.users.nicknameTag))).from(sketchbook)
                .where(sketchbook.isPublic.eq(true).and(sketchbook.isWritePossible.eq(false))
                                .and(select(letter.count())
                                        .from(letter)
                                        .where(letter.sketchbookCharacterMotion.sketchbook.eq(sketchbook))
                                        .goe(4L)))
                .orderBy(Expressions.numberTemplate(Double.class, "function('RAND')").asc())
                .fetchFirst();
    }

    @Override
    public Optional<Sketchbook> findRepresentSkechbook(Integer userId) {
        Sketchbook represent = query
                .select(sketchbook)
                .from(sketchbook)
                .where(sketchbook.isRepresent.eq(true).and(sketchbook.users.id.eq(userId)))
                .fetchOne();
        return Optional.ofNullable(represent);
    }

    @Override
    public boolean existsRepresentSketchbook(Integer userId) {
        Integer count = query
                .select(sketchbook.count())
                .from(sketchbook)
                .where(sketchbook.isRepresent.isTrue().and(sketchbook.users.id.eq(userId)))
                .fetchFirst() != null ? 1 : 0;
        return count > 0;
    }



}

