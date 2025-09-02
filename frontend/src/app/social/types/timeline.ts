export interface TimelineItem {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  imageUrl?: string; 
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  user: {
    id: number;
    nickname: string;
    profileImageUrl?: string;
  };

  contentType?: string;
  rating?: number;
  isPublic?: boolean;
  tagNames?: string[];
  releasedAt?: string;

  posterUrl?: string;
  type?: string;
}
