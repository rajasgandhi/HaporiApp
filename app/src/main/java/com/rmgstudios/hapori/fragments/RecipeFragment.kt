package com.rmgstudios.hapori.fragments

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.rmgstudios.hapori.R
import com.rmgstudios.hapori.RecipeResults
import com.rmgstudios.hapori.helpers.RecipeListData
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class RecipeFragment : Fragment() {
    fun RecipeFragment() {
        // Required empty public constructor
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_recipe, container, false)
        val getRecipesBtn = view.findViewById<Button>(R.id.getRecipesBtn)
        val ingredientInput = view.findViewById<EditText>(R.id.ingredientInput)
        val recipeList1 = ArrayList<RecipeListData>()

        getRecipesBtn.setOnClickListener {

            if (ingredientInput.text.isNullOrBlank()) {
                Toast.makeText(
                    requireActivity(),
                    "Please make sure your ingredients are not blank!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("https://api.spoonacular.com/recipes/complexSearch?apiKey=" + getString(R.string.spoonacularApiKey) + "&diet=vegetarian&includeIngredients=" + ingredientInput.text)
                    .get()
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                Toast.makeText(
                                    requireActivity(),
                                    "Oops! An error occurred, please make sure you have vegetarian ingredients!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                throw IOException("Unexpected code $response")
                            }

                            recipeList1.clear()

                            val responseBody = response.body!!.string()

                            val jsonObject = JSONObject(responseBody)
                            val results = jsonObject.getJSONArray("results")
                            val resultLength = results.length()

                            for (i in 0 until resultLength) {
                                recipeList1.add(
                                    RecipeListData(
                                        (results[i] as JSONObject).getString("title"),
                                        (results[i] as JSONObject).getString("image"),
                                        (results[i] as JSONObject).getInt("id")
                                    )
                                )
                            }
                            if (recipeList1.isEmpty()) {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(
                                        requireActivity(),
                                        "Oops! An error occurred, please make sure you have vegetarian ingredients!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    val intent =
                                        Intent(requireActivity(), RecipeResults::class.java)
                                    intent.putExtra("RECIPE_LIST", recipeList1)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                })
            }
        }
        changeTitleTextSize(view.findViewById(R.id.recipeGeneratorText))
        return view
    }

    private fun changeTitleTextSize(title: TextView) {
        val displayMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireActivity().display!!.getRealMetrics(displayMetrics)
        } else requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels

        title.textSize = (.037037 * width).toFloat()
    }
}