package com.dev.community.ui.start

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.dev.community.R


class ExpandableListAdapter(
    private val context: Context,
    private val parents: MutableList<String>,
    private val childList: MutableList<MutableList<String>>,
) : BaseExpandableListAdapter() {

    override fun getGroupCount() = parents.size

    override fun getChildrenCount(parent: Int) = childList[parent].size

    override fun getGroup(parent: Int) = parents[parent]

    override fun getChild(parent: Int, child: Int): String = childList[parent][child]

    override fun getGroupId(parent: Int) = parent.toLong()

    override fun getChildId(parent: Int, child: Int) = child.toLong()

    override fun hasStableIds() = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    // 부모 계층 레이아웃 설정
    override fun getGroupView(
        parent: Int,
        isExpanded: Boolean,
        convertView: View?,
        parentview: ViewGroup,
    ): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val binding = inflater.inflate(R.layout.menu_parent, parentview, false)

        val parentTitle = binding.findViewById<TextView>(R.id.parent_city_tv)
        parentTitle.text = parents[parent]

        setIcon(parent, binding)
        setArrow(binding, isExpanded)

        return binding
    }

    // 자식 계층 레이아웃 설정
    override fun getChildView(
        parent: Int,
        child: Int,
        isLastChild: Boolean,
        convertView: View?,
        parentview: ViewGroup,
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = inflater.inflate(R.layout.menu_child, parentview, false)

        val childTitle = binding.findViewById<TextView>(R.id.child_city_tv)
        childTitle.text = getChild(parent, child)

        return binding
    }

    // drawer 아이콘 설정
    private fun setIcon(parentPosition: Int, parentView: View) {
        val cityIcon = parentView.findViewById<ImageView>(R.id.icon_iv)
        when (parentPosition) {

            0 -> cityIcon.setImageResource(R.drawable.icon_village)
        }
    }

    // 닫힘, 열림 표시해주는 화살표 설정
    @SuppressLint("SuspiciousIndentation")
    private fun setArrow(parentView: View, isExpanded: Boolean) {

        val arrowIcon = parentView.findViewById<ImageView>(R.id.arrow_drop_iv)
        if (isExpanded) arrowIcon.setImageResource(R.drawable.icon_arrow_up)
        else arrowIcon.setImageResource(R.drawable.icon_arrow_drop)
    }
}