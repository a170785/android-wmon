/* autogenerated by pidl */

/* DO NOT EDIT
	This filter was automatically generated
	from eventlog.idl and eventlog.cnf.
	
	Pidl is a perl based IDL compiler for DCE/RPC idl files. 
	It is maintained by the Samba team, not the Wireshark team.
	Instructions on how to download and install Pidl can be 
	found at http://wiki.wireshark.org/Pidl

	$Id: packet-dcerpc-eventlog.h 34718 2010-10-30 16:29:23Z morriss $
*/


#ifndef __PACKET_DCERPC_EVENTLOG_H
#define __PACKET_DCERPC_EVENTLOG_H

int eventlog_dissect_bitmap_eventlogReadFlags(tvbuff_t *tvb _U_, int offset _U_, packet_info *pinfo _U_, proto_tree *tree _U_, guint8 *drep _U_, int hf_index _U_, guint32 param _U_);
int eventlog_dissect_bitmap_eventlogEventTypes(tvbuff_t *tvb _U_, int offset _U_, packet_info *pinfo _U_, proto_tree *tree _U_, guint8 *drep _U_, int hf_index _U_, guint32 param _U_);
int eventlog_dissect_struct_OpenUnknown0(tvbuff_t *tvb _U_, int offset _U_, packet_info *pinfo _U_, proto_tree *parent_tree _U_, guint8 *drep _U_, int hf_index _U_, guint32 param _U_);
int eventlog_dissect_struct_Record(tvbuff_t *tvb _U_, int offset _U_, packet_info *pinfo _U_, proto_tree *parent_tree _U_, guint8 *drep _U_, int hf_index _U_, guint32 param _U_);
int eventlog_dissect_struct_ChangeUnknown0(tvbuff_t *tvb _U_, int offset _U_, packet_info *pinfo _U_, proto_tree *parent_tree _U_, guint8 *drep _U_, int hf_index _U_, guint32 param _U_);
#endif /* __PACKET_DCERPC_EVENTLOG_H */
