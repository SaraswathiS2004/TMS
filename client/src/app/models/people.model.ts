// The three types of relationship a person can have
export type RelationType = 'CLOSE' | 'DISTANCE' | 'FRIENDS';

// The four invitation statuses a person can hold
export type InvitedStatus = 'NOT_INVITED' | 'MARRIAGE_INVITED' | 'ENGAGEMENT_INVITED' | 'BOTH_INVITED';

// Represents a person in the invitation list
export interface People {
  id?: number;
  name: string;
  city: string;
  numberOfPerson: number;
  relationType: RelationType;
  invitedStatus: InvitedStatus;
}

// API response returned after add / update operations
export interface ApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
}
