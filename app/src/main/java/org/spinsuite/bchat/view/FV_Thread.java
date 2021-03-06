/*************************************************************************************
 * Product: Spin-Suite (Making your Business Spin)                                   *
 * This program is free software; you can redistribute it and/or modify it           *
 * under the terms version 2 of the GNU General Public License as published          *
 * by the Free Software Foundation. This program is distributed in the hope          *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied        *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                  *
 * See the GNU General Public License for more details.                              *
 * You should have received a copy of the GNU General Public License along           *
 * with this program; if not, write to the Free Software Foundation, Inc.,           *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                            *
 * For the text or an alternative of this public license, you may reach us           *
 * Copyright (C) 2012-2015 E.R.P. Consultores y Asociados, S.A. All Rights Reserved. *
 * Contributor(s): Yamel Senih www.erpcya.com                                        *
 *************************************************************************************/
package org.spinsuite.bchat.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.spinsuite.base.DB;
import org.spinsuite.base.R;
import org.spinsuite.bchat.adapters.BChatThreadAdapter;
import org.spinsuite.bchat.util.BCMessageHandle;
import org.spinsuite.bchat.util.BCNotificationHandle;
import org.spinsuite.bchat.util.DisplayBChatThreadItem;
import org.spinsuite.mqtt.connection.MQTTConnection;
import org.spinsuite.mqtt.connection.MQTTDefaultValues;
import org.spinsuite.sync.content.Invited;
import org.spinsuite.sync.content.SyncMessage_BC;
import org.spinsuite.sync.content.SyncRequest_BC;
import org.spinsuite.sync.content.SyncStatus;
import org.spinsuite.util.AttachmentHandler;
import org.spinsuite.util.Env;
import org.spinsuite.util.SerializerUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * 
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com Apr 6, 2015, 9:54:42 PM
 *
 */
public class FV_Thread extends Fragment {

	/**
	 * 
	 * *** Constructor ***
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 01/04/2014, 17:42:27
	 */
    public FV_Thread(){

    }
    
    /**
     * 
     * *** Constructor ***
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_ctx

    public FV_Thread(Context p_ctx){
    	m_ctx = p_ctx;
    }*/ //red1

    /**	View 						*/
	private View 						m_view 				= null;
	/**	List View					*/
	private static ListView				lv_Thread			= null;
	/**	Message						*/
	private EditText					et_Message 			= null;
	/**	Button Send					*/
	private ImageButton					ib_Send				= null;
	/**	Request						*/
	private static SyncRequest_BC 		m_Request			= null;
	/**	Is Active					*/
	private static boolean				m_IsActive			= false;
	/**	Thread Adapter				*/
	private static BChatThreadAdapter	m_ThreadAdapter		= null;
	/**	Reload Data					*/
	private boolean						m_Reload			= true;
	/**	Context						*/
	private static Context				m_ctx 				= null;
	/**	Attach Handler				*/
	private AttachmentHandler			m_AttHandler		= null;
	/**	Action Bar					*/
	private static ActionBar			m_ActionBar			= null;
	/**	Temp Data URI				*/
	private Uri 						m_DataUri 			= null;
	
	/**	Conversation Type Constants	*/
	public static final int				CT_REQUEST			= 0;
	public static final int				CT_CHAT				= 1;
	
	/**	Results						*/
	public static final int 			ACTION_TAKE_PHOTO	= 3;
	public static final int 			ACTION_PICK_IMAGE	= 4;
	public static final int 			ACTION_PICK_FILE	= 5;
	
	/**	Constants Type Save			*/
	private static final String 		PHOTO_ATTACHMENT_SAVE	= "PS";
//	private static final String 		FILE_ATTACHMENT_SAVE	= "FS";
	private static final String 		IMAGE_ATTACHMENT_SAVE	= "IS";
	
	/**	Handler						*/
	public static Handler 				UIHandler;
	
    static {
        UIHandler = new Handler(Looper.getMainLooper());
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable); 
    }


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		m_ActionBar = activity.getActionBar();
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		if(m_view != null)
			return m_view;
		//	Inflate
		m_view =  inflater.inflate(R.layout.v_business_chat_thread, container, false);
    	//	Scroll
		lv_Thread 	= (ListView) m_view.findViewById(R.id.lv_Thread);
		et_Message 	= (EditText) m_view.findViewById(R.id.et_Message);
		ib_Send 	= (ImageButton) m_view.findViewById(R.id.ib_Send);
		//	Listener
		ib_Send.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(et_Message.getText() == null
						|| et_Message.getText().toString().trim().length() == 0)
					return;
				//	Send Message
				sendMessage(null);
			}
		});
		//	Hide Separator
		lv_Thread.setDividerHeight(0);
		lv_Thread.setDivider(null);
		lv_Thread.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv_Thread.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View arg1, int position,
					long arg3) {
				DisplayBChatThreadItem item = (DisplayBChatThreadItem) m_ThreadAdapter.getItem(position);
				//	Show Image
				String fileName = item.getFileName();
				if(fileName != null
						&& fileName.length() > 0) {
					String m_FilePath = Env.getBC_IMG_DirectoryPathName(m_ctx);
					File file = new File(m_FilePath + File.separator + fileName);
					//	Show
					AttachmentHandler.showAttachment(m_ctx, Uri.fromFile(file));
				}
			}
        });
		//	
		lv_Thread.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// Capture total checked items
				final int checkedCount = lv_Thread.getCheckedItemCount();
				// Set the CAB title according to total checked items
				mode.setTitle(checkedCount + " " + getString(R.string.BChat_Selected));
				// Calls toggleSelection method from ListViewAdapter Class
				m_ThreadAdapter.toggleSelection(position);
				
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
					case R.id.action_delete:
						SparseBooleanArray selectedItems = m_ThreadAdapter.getSelectedItems();
						StringBuffer inClause = new StringBuffer();
						for (int i = (selectedItems.size() - 1); i >= 0; i--) {
							if (selectedItems.valueAt(i)) {
								DisplayBChatThreadItem selectedItem = m_ThreadAdapter
										.getItem(selectedItems.keyAt(i));
								//	Add Separator
								if(inClause.length() > 0) {
									inClause.append(", ");
								}
								//	Add Value
								inClause.append("'").append(selectedItem.getSPS_BC_Message_UUID()).append("'");
								//	Remove Item
								m_ThreadAdapter.remove(selectedItem);
							}
						}
						//	Delete Records in DB
						if(inClause.length() > 0) {
							BCMessageHandle.getInstance(m_ctx).deleteMessage(m_Request, 
									"SPS_BC_Message_UUID IN(" + inClause.toString() + ")");
						}
						mode.finish();
						return true;
					case R.id.action_copy:
						selectedItems = m_ThreadAdapter.getSelectedItems();
						boolean justOne = selectedItems.size() == 1; 
						StringBuffer text = new StringBuffer();
						for (int i = (selectedItems.size() - 1); i >= 0; i--) {
							if (selectedItems.valueAt(i)) {
								DisplayBChatThreadItem selectedItem = m_ThreadAdapter
										.getItem(selectedItems.keyAt(i));
								//	Valid File
								if(selectedItem.getFileName() != null)
									continue;
								//	Add New Line
								if(text.length() > 0) {
									text.append(Env.NL);
								}
								//	Add to Text
								if(justOne) {
									text.append(selectedItem.getText());
								} else {
									text.append(selectedItem.getCopy());
								}
							}
						}
						//	Add To Clipboard
						if(text.length() > 0) {
							 Env.setClipboardText(m_ctx, text.toString());
						}
						mode.finish();
						return true;
					default:
						return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.bc_thread_selected, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				m_ThreadAdapter.removeSelection();
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
		});
		//	Return
		return m_view;
	}
    
    /**
     * Send Message
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return void
     */
    private void sendMessage(String p_FileName) {
		//	Send Request
		if(m_Request != null
    			&& m_Request.getSPS_BC_Request_UUID() == null) {
			boolean ok = BCMessageHandle.getInstance(m_ctx).sendRequest(m_Request);
			//	Add UI Request
			if(ok) {
				BCNotificationHandle.getInstance(m_ctx)
					.addRequest(m_Request);
			}
		}
		//	
		byte[] bytes = SerializerUtil.getFromFile(
				Env.getBC_IMG_DirectoryPathName(m_ctx) + File.separator + p_FileName);
		//	Send Message
		SyncMessage_BC message = new SyncMessage_BC(null, MQTTConnection.getClient_ID(m_ctx), 
				m_Request.getSPS_BC_Request_UUID(), Env.getAD_User_ID(), 
				Env.getContext("#AD_User_Name"), 
				et_Message.getText().toString(), p_FileName, bytes);
		//	Send Message
		BCMessageHandle.getInstance(m_ctx).sendMsg(message);
		//	Add Message
		addMsg(message, MQTTDefaultValues.TYPE_OUT);
		seekToLastMsg();
		//	Clear Data
		et_Message.setText("");
		//	
		m_Reload = true;
    }
    
    /**
     * Seek to Last Message
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return void
     */
    public static void seekToLastMsg() {
    	lv_Thread.setSelection(m_ThreadAdapter.getCount() - 1);
    }
    
    /**
     * Load List
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com 01/04/2014, 21:11:36
     * @return
     * @return boolean
     */
    private boolean loadData() {
    	//	Verify if is reload data
    	if(!m_Reload) {
    		return false;
    	}
    	m_Reload = false;
    	if(m_Request != null
    			&& m_Request.getSPS_BC_Request_UUID() == null) {
    		et_Message.setText(getString(R.string.BChat_Hi) + " " 
    			+ m_Request.getName() + ", " 
    			+ getString(R.string.BChat_NewRequest));
    		m_ThreadAdapter = new BChatThreadAdapter(getActivity(), 
    				new ArrayList<DisplayBChatThreadItem>(), m_Request.isGroup());
    		//	
    	} else if(m_Request != null) {
    		//	Get Data
    		m_ThreadAdapter = new BChatThreadAdapter(getActivity(), 
    				getData(), m_Request.isGroup());
    		//	Send New Status
    		BCMessageHandle.getInstance(m_ctx)
    			.sendStatus(m_Request.getSPS_BC_Request_UUID(), SyncStatus.STATUS_IN_CHAT);
    	}
    	//	
    	lv_Thread.setAdapter(m_ThreadAdapter);
		lv_Thread.setSelection(m_ThreadAdapter.getCount() - 1);
		//	Change Title
		getActivity().getActionBar().setTitle(m_Request.getName());
		getActivity().getActionBar().setSubtitle(m_Request.getLastMsg());
    	//	Return
        return true;
    }
    
    /**
     * Get Data for Chat
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return
     * @return ArrayList<DisplayBChatThreadItem>
     */
    private ArrayList<DisplayBChatThreadItem> getData() {
		//	Instance Data
		ArrayList<DisplayBChatThreadItem> data = new ArrayList<DisplayBChatThreadItem>();
		//	Valid Request
    	if(m_Request == null)
    		return data;
    	//	Create Connection
    	DB conn = DB.loadConnection(getActivity(), DB.READ_ONLY);
    	//	Compile Query
    	conn.compileQuery("SELECT "
    			+ "m.SPS_BC_Message_UUID, "
    			+ "m.SPS_BC_Request_UUID, "
    			+ "m.AD_User_ID, "
    			+ "u.Name UserName, "
    			+ "m.Text, "
    			+ "m.Type, "
    			+ "m.Status, "
    			+ "(strftime('%s', m.Updated)*1000) Updated, "
    			+ "m.FileName "
    			+ "FROM SPS_BC_Message m "
    			+ "INNER JOIN AD_User u ON(u.AD_User_ID = m.AD_User_ID) "
    			+ "WHERE m.SPS_BC_Request_UUID = ? "
    			+ "ORDER BY m.Updated");
    	//	Add Parameter
    	conn.addString(m_Request.getSPS_BC_Request_UUID());
    	//	Load Data
    	Cursor rs = conn.querySQL();
    	//	Valid Result set
    	if(rs != null 
    			&& rs.moveToFirst()) {
    		int col = 0;
    		//	Loop
    		do {
    			data.add(new DisplayBChatThreadItem(
    					rs.getString(col++), 
    					rs.getString(col++), 
    					rs.getInt(col++), 
    					rs.getString(col++), 
    					rs.getString(col++), 
    					rs.getString(col++), 
    					rs.getString(col++), 
    					new Date(rs.getLong(col++)), 
    					rs.getString(col++), 
    					null));
    			//	Set Column
    			col = 0;
    		} while(rs.moveToNext());
    	}
    	//	Close Connection
    	DB.closeConnection(conn);
    	//	Return
    	return data;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	m_IsActive = true;
    	clearNotification();
    }
    
    /**
     * Clear Notification
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return void
     */
    private void clearNotification() {
    	NotificationManager m_NotificationManager = (NotificationManager) m_ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    	m_NotificationManager.cancel(MQTTDefaultValues.NOTIFICATION_ID);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	m_IsActive = false;
    }
    
    /**
     * Select a Conversation
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_SPS_BC_Request_UUID
     * @return void
     */
    public void selectConversation(String p_SPS_BC_Request_UUID) {
    	m_Request = BCMessageHandle.getInstance(m_ctx).getRequest(p_SPS_BC_Request_UUID);
    	//	Set Reload Data
    	m_Reload = true;
    	if(m_view != null
    			&& m_Request != null) {
    		loadData();
    	}
    }
    
    /**
     * Add New Message
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param msg
     * @return void
     */
    public static void addMsg(DisplayBChatThreadItem msg) {
    	m_ThreadAdapter.add(msg);
    	m_ThreadAdapter.notifyDataSetChanged();
    }
    
    /**
     * Add Message
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param message
     * @param p_Type
     * @return void
     */
    public static void addMsg(SyncMessage_BC message, String p_Type) {
    	addMsg(new DisplayBChatThreadItem(message.getSPS_BC_Message_UUID(),  message.getSPS_BC_Request_UUID(), 
								message.getAD_User_ID(), message.getUserName(), 
								message.getText(),
								p_Type, 
								MQTTDefaultValues.STATUS_CREATED, 
								new Date(System.currentTimeMillis()), 
								message.getFileName(), 
								message.getAttachment()));
    }
    
    /**
     * Change Message
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return void
     */
    public static void changeMsgStatus(String p_SPS_BC_Message_UUID, String p_Status) {
    	m_ThreadAdapter.changeMsgStatus(p_SPS_BC_Message_UUID, p_Status);
    	m_ThreadAdapter.notifyDataSetChanged();
    }
    
    /**
     * Change Connection Status, Optional Request for when is typing
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_Status
     * @return void
     */
    public static void changeConnectionStatus(SyncStatus p_Status) {
		//	Validate Request
		if(isOpened(p_Status.getSPS_BC_Request_UUID())
				|| existsUser(p_Status.getAD_User_ID())) {
			changeStatus(p_Status.getStatus());
		}
    }
    
    /**
     * Change Status
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_Status
     * @return void
     */
    private static void changeStatus(String p_Status) {
    	if(m_ActionBar != null
    			&& p_Status != null) {
    		int message = R.string.BChat_StatusDisconnected;
    		//	
    		if(p_Status.equals(SyncStatus.STATUS_CONNECTED)) {
    			message = R.string.BChat_StatusConnected;
    		} else if(p_Status.equals(SyncStatus.STATUS_TYPING)) {
    			message = R.string.BChat_StatusTyping;
    		} else if(p_Status.equals(SyncStatus.STATUS_IN_CHAT)) {
    			message = R.string.BChat_StatusInChat;
    		}
    		//	Set Message
    		m_ActionBar.setSubtitle(m_ctx.getString(message));
    	}
    }
    
    /**
     * Verify if is open thread
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_SPS_BC_Request_UUID
     * @return
     * @return boolean
     */
    public static boolean isOpened(String p_SPS_BC_Request_UUID) {
    	if(m_Request == null)
    		return false;
    	//	Valid Request Parameter
    	if(p_SPS_BC_Request_UUID == null)
    		return false;
    	//	Valid Null Request
    	if(m_Request.getSPS_BC_Request_UUID() == null)
    		return false;
    	//	Valid Opened
    	return (m_Request.getSPS_BC_Request_UUID().equals(p_SPS_BC_Request_UUID)
    			&& m_IsActive);
    }
    
    /**
     * Validate if Exists User
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_AD_User_ID
     * @return
     * @return boolean
     */
    public static boolean existsUser(int p_AD_User_ID) {
    	if(m_Request == null)
    		return false;
    	//	Valid Request Parameter
    	if(p_AD_User_ID == -1)
    		return false;
    	//	Valid same user
    	if(p_AD_User_ID == Env.getAD_User_ID())
    		return false;
    	//	Valid Opened
    	for(Invited invited : m_Request.getUsers()) {
    		if(invited.getAD_User_ID() == p_AD_User_ID) {
    			return true;
    		}
    	}
    	//	Default Return
    	return false;
    }
    
    
    /**
     * Select a User for request
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param p_AD_User_ID
     * @param p_Name
     * @return void
     */
    public void requestUser(int p_AD_User_ID, String p_Name) {
    	//	For Request
    	if(p_AD_User_ID != 0
    			&& p_AD_User_ID != -1) {
			String m_TopicName = DB.getSQLValueString(m_ctx, 
					"SELECT r.SPS_BC_Request_UUID FROM SPS_BC_Request r "
					+ "WHERE r.Name = ?", new String[]{p_Name});
			//	
			if(m_TopicName != null) {
				m_Request = BCMessageHandle.getInstance(m_ctx).getRequest(m_TopicName);
			} else {
				m_Request = new SyncRequest_BC(null, 
    					String.valueOf(Env.getAD_User_ID()), 
    					null, 
    					p_Name, 
    					null, null, false);
				//	Add User to Request
				m_Request.addUser(new Invited(p_AD_User_ID, MQTTDefaultValues.STATUS_CREATED));
				m_Request.addUser(new Invited(Env.getAD_User_ID(), MQTTDefaultValues.STATUS_CREATED));
			}
		}
    	//	Set Reload Data
    	m_Reload = true;
    	if(m_view != null) {
    		loadData();
    	}
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.bc_thread, menu);
		//	Get Item
		MenuItem item = menu.findItem(R.id.action_search);
		//	Search View
		final View searchView = SearchViewCompat.newSearchView(m_ctx);
		if (searchView != null) {
			//	Set Back ground Color
			int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
			EditText searchText = (EditText) searchView.findViewById(id);
			//	Set Parameters
			if(searchText != null)
				searchText.setTextAppearance(m_ctx, R.style.TextSearch);
			//	
			SearchViewCompat.setOnQueryTextListener(searchView,
					new OnQueryTextListenerCompat() {
				@Override
				public boolean onQueryTextChange(String newText) {
					if(m_ThreadAdapter != null) {
						String mFilter = !TextUtils.isEmpty(newText) ? newText : null;
						m_ThreadAdapter.getFilter().filter(mFilter);
					}
					return true;
				}
			});
			SearchViewCompat.setOnCloseListener(searchView,
					new OnCloseListenerCompat() {
				@Override
				public boolean onClose() {
					if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
						SearchViewCompat.setQuery(searchView, null, true);
					}
					return true;
				}
                    
			});
			MenuItemCompat.setActionView(item, searchView);
		}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
			case R.id.action_attach_photo:
				attachCapture();
				return true;
			case R.id.action_attach_image:
				attachFile(ACTION_PICK_IMAGE);
				return true;
			case R.id.action_attach_file:
				attachFile(ACTION_PICK_FILE);
				return true;
			//	Default
			default:
				return super.onOptionsItemSelected(item);
    	}
    }
    
    /**
     * Attach Cature
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @return void
     */
    private void attachCapture() {
    	//	Instance Attachment
    	if(m_AttHandler == null) {
    		m_AttHandler = new AttachmentHandler(getActivity());
    	}
    	//	
    	//	Delete Temp File
    	File tmpFile = new File(m_AttHandler.getTMPImageName());
    	if(tmpFile.exists()) {
    		if(!tmpFile.delete())
    			tmpFile.deleteOnExit();
    	}
    	//	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
	    getActivity().startActivityForResult(intent, ACTION_TAKE_PHOTO);
	}
    
    /**
     * Attach File
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
     * @param action
     * @return void
     */
    private void attachFile(int action) {
    	//	Instance Attachment
    	if(m_AttHandler == null) {
    		m_AttHandler = new AttachmentHandler(getActivity());
    	}
    	String type = null;
    	if(action == ACTION_PICK_IMAGE) {
    		type = "image/*";
    	} else if(action == ACTION_PICK_FILE) {
    		type = "file/*";
    	} else {
    		return;
    	}
    	//	
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType(type);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	getActivity().startActivityForResult(intent, action);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//	Valid Data
    	if(requestCode == ACTION_TAKE_PHOTO) {
    		new SaveTask().execute(PHOTO_ATTACHMENT_SAVE);
    	} else if(requestCode == ACTION_PICK_IMAGE
    			&& data != null) {
    		m_DataUri = data.getData();
    		new SaveTask().execute(IMAGE_ATTACHMENT_SAVE, data.getData().getPath());
    	}
    }
    
    /**
     * Async Task for Save File
     * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com Apr 16, 2015, 9:33:44 AM
     *
     */
    private class SaveTask extends AsyncTask<String, Void, Void> {

		/**	Progress Bar			*/
		private ProgressDialog 		v_PDialog;
		private String				m_Type 	= null;
		private String				m_FileName = null;
		private boolean				m_IsSaved = false;
		
		@Override
		protected void onPreExecute() {
			v_PDialog = ProgressDialog.show(m_ctx, null, 
					getString(R.string.msg_Saving), false, false);
			//	Set Max
		}
		
		@Override
		protected Void doInBackground(String... params) {
			m_Type = params[0];
			//	Valid Null
			if(m_Type == null)
				return null;
			//	
			if(m_Type.equals(PHOTO_ATTACHMENT_SAVE)) {
				String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				m_IsSaved = m_AttHandler.processImgAttach(
						Env.getBC_IMG_DirectoryPathName(getActivity()), fileName, AttachmentHandler.IMG_STD_Q);
				m_FileName = fileName + AttachmentHandler.JPEG_FILE_SUFFIX;
			} else if(m_Type.equals(IMAGE_ATTACHMENT_SAVE)) {
				String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss")
											.format(new Date()) + AttachmentHandler.JPEG_FILE_SUFFIX;
				Bitmap image = m_AttHandler.getBitmapFromUri(m_DataUri);
				m_IsSaved = m_AttHandler.saveImageBitmap(image, 
						Env.getBC_IMG_DirectoryPathName(getActivity()), fileName);
				m_FileName = fileName;
			}
			//	
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			
		}
		
		@Override
		protected void onPostExecute(Void result) {
			//	Hide
			v_PDialog.dismiss();
			//	Save File
			if(m_IsSaved) {
				sendMessage(m_FileName);
			}
		}
	}
    
}