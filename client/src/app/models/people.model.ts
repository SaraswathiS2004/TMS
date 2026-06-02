export type RelationType = 'CLOSE_RELATIVE' | 'DISTANCE_RELATIVE' | 'FRIENDS';

export interface InvitationPerson {
  id?: number;
  name: string;
  note?: string;
  // per-function invited status for this individual person (functionId as string → "INVITED" | "NOT_INVITED")
  functionStatuses?: { [fnId: string]: string };
}

export interface People {
  id?: number;
  name: string;
  city: string;
  numberOfPerson: number;
  relationType: RelationType;
  invitedFunctionIds: number[];
  // key is functionId as string (JSON map keys are always strings)
  functionStatuses?: { [fnId: string]: string };
  // named persons (guests) listed under this invitation
  persons?: InvitationPerson[];
}

export interface ApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
}
