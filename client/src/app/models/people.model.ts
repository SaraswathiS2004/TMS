export type RelationType = 'CLOSE_RELATIVE' | 'DISTANCE_RELATIVE' | 'FRIENDS';

export interface People {
  id?: number;
  name: string;
  city: string;
  numberOfPerson: number;
  relationType: RelationType;
  invitedFunctionIds: number[];
}

export interface ApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
}
