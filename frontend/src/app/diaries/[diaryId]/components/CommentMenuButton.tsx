"use client";

import { useEffect, useState, useRef } from "react";

export default function CommentMenuButton({
  onEdit,
  onDelete,
}: {
  onEdit: () => void;
  onDelete: () => void;
}) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-all duration-200"
        onClick={() => setOpen((prev) => !prev)}
        aria-label="댓글 옵션"
      >
        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
        </svg>
      </button>

      {open && (
        <div className="absolute right-0 top-12 w-36 bg-white border border-gray-200 rounded-2xl shadow-xl z-30 overflow-hidden animate-in fade-in duration-200">
          <button
            onClick={() => {
              setOpen(false);
              onEdit();
            }}
            className="w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 text-left transition-colors duration-200 font-medium"
          >
            수정하기
          </button>
          <div className="border-t border-gray-100" />
          <button
            onClick={() => {
              setOpen(false);
              onDelete();
            }}
            className="w-full px-4 py-3 text-sm text-red-600 hover:bg-red-50 text-left transition-colors duration-200 font-medium"
          >
            삭제하기
          </button>
        </div>
      )}
    </div>
  );
}