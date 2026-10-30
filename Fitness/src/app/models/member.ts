export type MemberStatus = 'ACTIVE' | 'FROZEN' | 'EXPIRED' | 'INACTIVE';

export interface Member {
  id: number; firstName: string; lastName: string; email: string;
  phone?: string; birthDate?: string; age?: number; sex?: 'MALE' | 'FEMALE' | 'OTHER';
  goal?: string; status: MemberStatus; joinedAt: string; hasAccount: boolean;
}
export type MemberRequest = Omit<Member, 'id' | 'age' | 'status' | 'joinedAt' | 'hasAccount'>;

export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }