package com.rad4m.eventdo.ui.introfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rad4m.eventdo.databinding.FragmentFirstIntroBinding
import com.rad4m.eventdo.databinding.FragmentSecondIntroBinding

class IntroAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ViewHolder.from(parent)
            else -> ViewHolder2.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder private constructor(binding: FragmentFirstIntroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    FragmentFirstIntroBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class ViewHolder2 private constructor(binding: FragmentSecondIntroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder2 {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    FragmentSecondIntroBinding.inflate(layoutInflater, parent, false)
                return ViewHolder2(binding)
            }
        }
    }
}