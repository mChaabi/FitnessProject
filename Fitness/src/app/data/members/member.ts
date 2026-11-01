export type Sex = 'MALE' | 'FEMALE' | 'OTHER'; // Adjust according to your Java Sex enum values

export type MemberStatus = 'ACTIVE' | 'FROZEN' | 'EXPIRED' | 'INACTIVE';

export interface MemberRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  birthDate?: string; // LocalDate as 'YYYY-MM-DD' string
  sex?: Sex;
  goal?: string;
}

export interface Member {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  birthDate?: string;
  sex?: Sex;
  goal?: string;
  status: MemberStatus;
  qrCode: string;
  user?: any;        // Replace with a User interface if you have one
  coach?: any;       // Replace with a Coach interface if you have one
  joinedAt: string;  // LocalDate as string
  emailOptOut: boolean;
}

// Optional helper interface if your Spring Boot backend returns Spring Data Page objects
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}