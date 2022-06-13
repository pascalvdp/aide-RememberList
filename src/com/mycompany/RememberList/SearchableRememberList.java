package com.mycompany.RememberList;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.widget.AdapterView.*;
import android.database.*;
import android.widget.SearchView.*;

//enkel via externalstorage kan je je gegevens overzetten naar de gsm

public class SearchableRememberList extends Activity
{
	ListView lv;
	RememberListDatabase mDb;
	SearchView searchView1;
	
	private static final int DIALOG_DELETE_LIST=1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView1 = (SearchView) findViewById(R.id.edittext);
		searchView1.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView1.setIconifiedByDefault(false);
		
		Button msave=(Button)findViewById(R.id.savebutton);
		Button mdelete=(Button)findViewById(R.id.deletelistbutton);
		lv=findViewById(R.id.listview);
		mDb=new RememberListDatabase(this,isExternalStorageWritable());//isExternalStorageWritable()
		mDb.open();
		showlist1(searchView1.getQuery().toString());
		//int idtest=R.drawable.symb;
		msave.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (!searchView1.getQuery().toString().isEmpty() && searchView1.getQuery().toString().compareTo(" ")!=0){
						mDb.addText(searchView1.getQuery().toString());
						showlist1(searchView1.getQuery().toString()); 
						toast("New item saved");
					}else{
						toast("Nothing saved");
					}	
				}
		});
		
	/*	msave.setOnLongClickListener(new OnLongClickListener(){//via dit kunnen we de lijst copieren

				@Override
				public boolean onLongClick(View p1)
				{
					Cursor cursor=mDb.getAllText();
					mDb.open2();
					if(cursor.moveToFirst()){
						do{
							String str=cursor.getString(cursor.getColumnIndex(RememberListDatabase.KEY_TEXT));
							mDb.addText(str);
						}while(cursor.moveToNext());
					}
					cursor.close();
					toast("Copied list");
					return true;
				}
		});*/
		
		mdelete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					showDialog(DIALOG_DELETE_LIST);
				}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long id)
				{
					Cursor cursor=mDb.getText(String.valueOf(id),null);
					String str=cursor.getString(cursor.getColumnIndex(RememberListDatabase.KEY_TEXT));
					searchView1.setQuery(str,false);
					searchView1.requestFocus();
				}
		});
		
		searchView1.setOnQueryTextListener(new OnQueryTextListener(){

				@Override
				public boolean onQueryTextSubmit(String p1)
				{
					// TODO: Implement this method
					return false;
				}

				@Override
				public boolean onQueryTextChange(String query)
				{
					showlist1(query);
					return false;
				}
		});
    }
	
	private void showlist1(String query){
		Cursor cursor=null;
		if (query.compareTo("") == 0) {
			cursor=mDb.getAllText();
		}else{
			cursor = mDb.getTextMatches(query,null);
		}
		if (cursor == null) {
			lv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
        } else {
			lv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            // Specify the columns we want to display in the result
            String[] from = new String[] { RememberListDatabase.KEY_TEXT};

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.text};

            // Create a simple cursor adapter for the words and apply them to the ListView
		    SimpleCursorAdapter words = new SimpleCursorAdapter(this,
																R.layout.result, cursor, from, to);
			lv.setAdapter(words);
			lv.setOnItemLongClickListener(new OnItemLongClickListener(){

					@Override
					public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, final long id)
					{
						PopupMenu popup = new PopupMenu(SearchableRememberList.this, p2);
						popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
						popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){

								@Override
								public boolean onMenuItemClick(MenuItem item)
								{
									switch (item.getItemId()) {
										case R.id.item1:
											mDb.deleteText(id);
											showlist1(searchView1.getQuery().toString());
											toast("Item deleted from list");
											return true;
										default:
											return false;
									}	
								}
							});
						popup.show();
						return true;
					}
				});
        }
	}
	
	@Override
	protected void onDestroy() {
		//toast("onDestroy");
		super.onDestroy();
		if (mDb != null) {
			mDb.close();
		}
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
	
	public void toast(String pp){
		Toast.makeText(this,pp,Toast.LENGTH_SHORT).show();
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		//LayoutInflater layoutInflater = LayoutInflater.from(this);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_DELETE_LIST:
				builder
					.setTitle("Delete list?")
					.setOnCancelListener(new DialogInterface.OnCancelListener(){

						@Override
						public void onCancel(DialogInterface p1)
						{
							toast("List not deleted");
						}
					})	
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mDb.deleteTextList();
							showlist1(searchView1.getQuery().toString());
							toast("List deleted");
						}
					})
					.setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							dialog.cancel();
						}
					});
				AlertDialog dialogDelete = builder.create();
				dialogDelete.show();
				break;
		}			
		return super.onCreateDialog(id);
	}

		
}
