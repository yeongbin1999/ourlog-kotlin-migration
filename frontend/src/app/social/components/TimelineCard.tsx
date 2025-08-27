"use client";

import { Card, Image, Row, Col } from "react-bootstrap";
import { TimelineItem } from "../types/timeline";
import { FaHeart, FaRegHeart, FaComment } from "react-icons/fa";
import { useState } from "react";
import { useRouter } from "next/navigation";

interface Props {
  item: TimelineItem;
}

export default function TimelineCard({ item }: Props) {
  const [isLiked, setIsLiked] = useState(item.isLiked);
  const [likeCount, setLikeCount] = useState(item.likeCount);
  const router = useRouter();

  const handleLikeClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const method = isLiked ? "DELETE" : "POST";

      const response = await fetch(`/api/v1/likes/${item.id}`, {
          method,
      });

      if (!response.ok) throw new Error("좋아요 요청 실패");

      //  좋아요 후, 서버에서 최신 좋아요 수 조회
      const data = await response.json();

      setIsLiked(data.liked);
      setLikeCount(data.likeCount);
    } catch (err) {
      console.error("좋아요 요청 실패:", err);
      alert("좋아요 요청 중 오류가 발생했습니다.");
    }
  };

  const handleCardClick = () => {
    router.push(`/diaries/${item.id}`);
  };

  return (
    <Card
      className="shadow-sm rounded overflow-hidden mb-4 timeline-hover"
      style={{ width: "100%", maxWidth: "360px" }}
      onClick={handleCardClick}
    >
      {item.imageUrl && (
        <Card.Img
          variant="top"
          src={item.imageUrl}
          alt="Diary poster"
          style={{ height: "200px", objectFit: "cover" }}
        />
      )}

      <Card.Body>
        <Card.Title className="d-flex justify-content-between align-items-center">
          <span className="fw-bold">{item.title}</span>
        </Card.Title>
        <small className="text-muted">
          {new Date(item.createdAt).toLocaleDateString()}
        </small>
      </Card.Body>

      <Card.Footer className="bg-white border-0 pt-0">
        <Row className="align-items-center text-muted">

          {/* ❤️ 좋아요 버튼 + 숫자 */}
          <Col xs="auto" className="d-flex align-items-center gap-1">
            <span onClick={handleLikeClick} style={{ cursor: "pointer" }}>
              {isLiked ? (
                <FaHeart className="text-danger" />  // 좋아요 상태면 빨간 하트
              ) : (
                <FaRegHeart className="text-muted" />   // 아니라면 빈 하트
              )}
            </span>
            <span>{likeCount}</span>
          </Col>

          <Col xs="auto" className="d-flex align-items-center gap-1">
            <FaComment className="text-primary" />
            <span>{item.commentCount}</span>
          </Col>

          <Col className="d-flex align-items-center justify-content-end gap-2">
            {item.user.profileImageUrl && (
              <Image
                src={item.user.profileImageUrl}
                roundedCircle
                width={28}
                height={28}
                alt="profile"
              />
            )}
            <strong className="text-dark">{item.user.nickname}</strong>
          </Col>
        </Row>
      </Card.Footer>
    </Card>
  );
}
