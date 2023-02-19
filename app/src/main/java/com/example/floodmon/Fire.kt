package com.example.floodmon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Fire : AppCompatActivity() {
    private lateinit var dbref : DatabaseReference
    private lateinit var fRecyclerview : RecyclerView
    private lateinit var fArrayList : ArrayList<Locations>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firebase_flood_status)

        fRecyclerview = findViewById(R.id.locList)
        fRecyclerview.layoutManager = LinearLayoutManager(this)
        fRecyclerview.setHasFixedSize(true)

        arrayListOf<Locations>().also { fArrayList = it }
        getUserData()
    }

    private fun getUserData() {

        dbref = FirebaseDatabase.getInstance().getReference("Locations")

        dbref.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){

                    for (locSnapshot in snapshot.children){


                        val loc = locSnapshot.getValue(Locations::class.java)
                        val t=fArrayList.binarySearchBy(loc?.LocName) { it.LocName }
                        if(t!=-1) {
                            fArrayList.removeAt(t)
                        }
                        fArrayList.add(loc!!)


                    }

                    fRecyclerview.adapter = MyAdapter(fArrayList)


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }
}