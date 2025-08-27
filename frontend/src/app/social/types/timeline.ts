export interface TimelineItem {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  imageUrl?: string; // 피드 카드에 이미지가 없는 경우도 있기 때문에 ?(optional)
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  user: {
    id: number;
    nickname: string;
    profileImageUrl?: string;
  };
}
