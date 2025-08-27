'use client';

import { useParams } from 'next/navigation';
import UserProfileCard from '@/components/user/UserProfileCard';

export default function ProfilePage() {
  const { id } = useParams();

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50">
      <UserProfileCard userId={id as string} />
    </div>
  );
}
