export type RelationType = 'CLOSE_RELATIVE' | 'DISTANCE_RELATIVE' | 'FRIENDS';

export interface People {
  id?: number;
  name: string;
  city: string;
  numberOfPerson: number;
  relationType: RelationType;
  invitedFunctionIds: number[];
  // key is functionId as string (JSON map keys are always strings)
  functionStatuses?: { [fnId: string]: string };
}

export interface ApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
}
