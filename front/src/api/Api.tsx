import API, { ImgAPI } from "./Config";

//Authorization & User 관련 API

/** 로그아웃 */
export const postLogout = () => API.post(`/user/logout`);

/** 회원 탈퇴 */
export const deleteUser = () => API.delete(`/user`);

/** 유저 닉네임 조회 */
export const getUserNickname = () => API.get(`/user`).then((res) => res.data);

/** 유저 닉네임 변경
 * @param nickname 유저 닉네임
 */
export const postNickname = (nickname: string) =>
  API.post(`/user/nickname`, { nickname: nickname }).then((res) => res.data);

// 캐릭터 관련 API

/** 캐릭터 그림 전송(생성)
 * @param nickname 캐릭터 닉네임
 * @param file 캐릭터 png 파일
 */
export const postSketchCharacter = async (nickname: string, file: File) => {
  const formData = new FormData();

  formData.append("nickname", nickname);
  formData.append("file", file);

  // ImgAPI를 사용하여 요청 보내기
  try {
    const response = await ImgAPI.post(`/characters/create`, formData);
    return response.data;
  } catch (error) {
    console.log(error);
  }
};

/** 캐릭터 생성 취소
 * @param characterId 캐릭터 아이디
 */
export const cancelCharacter = (characterId: number) =>
  API.delete(`/characters/cancel`, { params: { characterId: characterId } });

/** 캐릭터 리스트 조회 */
export const getCharacterList = () =>
  API.get(`/characters/list`).then((res) => res.data.data);

/** 캐릭터 삭제
 * @param characterId 캐릭터 아이디
 */
export const deleteCharacter = (characterId: number) =>
  API.delete(`/characters/delete`, { params: { characterId: characterId } });

/** 대표캐릭터 선정
 * @param characterId 캐릭터 아이디
 */
export const patchMainCharacter = (characterId: number) =>
  API.patch(`/characters/my/maincharacter`, {},{
    params: { characterId: characterId },
  });

/** 캐릭터 닉네임 설정
 * @param characterId 캐릭터 아이디
 * @param nickname 캐릭터 닉네임
 */
export const patchCharacterNickname = (characterId: number, nickname: string) =>
  API.patch(`/characters/modify/nickname`, {
    params: { characterId: characterId, nickname: nickname },
  });

// 모션 관련 API

/** 모션 리스트 조회 */
export const getMotionList = () =>
  API.get(`/characters/list/motion`).then((res) => res.data);
// .then((res) => res.data);

/** 모션 선택
 * @param characterId 캐릭터 아이디
 * @param motionId 모션 아이디
 */
export const getMotionSelect = (characterId: number, motionId: number) => {
  return API.get(`/characters/select/motion`, {
    params: { characterId: characterId, motionId: motionId },
  }).then((res) => {
    return res.data;
  });
};

// 스케치북 관련 API

/** 스케치북 생성
 * @requires name  스케치북이름
 */
export const postSketchbook = (name: string) =>
  API.post(`/sketchbooks`, { name: name }).then((res) => res.data);

/** 스케치북 목록 조회 */
export const getSketchbookList = () =>
  API.get(`/sketchbooks/list`).then((res) => res.data);

/** 스케치북 목록 전체 조회 (임시)*/
export const getSketchbookListAll = () =>
  API.get(`/sketchbooks/all`).then((res) => res.data);

/** 스케치북 선택 간단 조회
 * @param sketchbookId 스케치북 아이디
 */
export const getSketchbookSelectedsimple = (uuid: string) =>
  API.get(`/sketchbooks/simple/${uuid}`).then((res) => res.data);

/** 스케치북 선택 상세 조회
 * @param sketchbookId 스케치북 아이디
 */
export const getSketchbookSelected = (uuid: string) =>
  API.get(`/sketchbooks/detail/${uuid}`).then((res) => res.data);

/** 스케치북 수정
 * @requires sketchbookId 스케치북 아이디
 * @requires name 스케치북 이름
 */
export const putSketchbookName = (sketchbookId: number, name: string) =>
  API.put(`/sketchbooks/${sketchbookId}`, { name: name }).then(
    (res) => res.data
  );

/** 스케치북 삭제
 * @requires sketchbookId 스케치북 아이디
 */
export const deleteSketchbook = (sketchbookId: number) =>
  API.delete(`/sketchbooks/${sketchbookId}`).then((res) => res.data);

// 편지 관련 API

/** 편지 작성
 * @requires content 편지 내용
 * @requires sketchbookId 스케치북 아이디
 * @requires characterMotionId 캐릭터 모션 ID
 */
export const postLetter = (
  content: string,
  sketchbookId: number,
  characterMotionId: number
) =>
  API.post(`/letter`, {
    content: content,
    sketchbookId: sketchbookId,
    characterMotionId: characterMotionId,
  }).then((res) => res.data);

/** 편지 목록 조회
 * @requires sketchbookId - 스케치북 ID
 */
export const getLetterList = (sketchbookId: number) =>
  API.get(`letter/list`, { params: { sketchbookId: sketchbookId } }).then(
    (res) => res.data
  );

/** 편지 삭제
 * @requires letterId - 편지 ID
 */
export const deleteLetter = (letterId: number) =>
  API.delete(`letter/${letterId}`).then((res) => res.data);
