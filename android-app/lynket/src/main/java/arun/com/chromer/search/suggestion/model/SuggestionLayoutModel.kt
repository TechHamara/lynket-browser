package arun.com.chromer.search.suggestion.model

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import androidx.core.text.toSpannable
import arun.com.chromer.R
import arun.com.chromer.extenstions.gone
import arun.com.chromer.extenstions.show
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionType.*
import arun.com.chromer.util.makeMatchingBold
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyAttribute.Option.DoNotHash
import com.airbnb.epoxy.EpoxyModelClass
import dev.arunkumar.android.epoxy.model.KotlinEpoxyModelWithHolder
import dev.arunkumar.android.epoxy.model.KotlinHolder
import kotlinx.android.synthetic.main.widget_suggestions_item_template.*

@EpoxyModelClass(layout = R.layout.widget_suggestions_item_template)
abstract class SuggestionLayoutModel :
  KotlinEpoxyModelWithHolder<SuggestionLayoutModel.ViewHolder>() {
  @EpoxyAttribute
  lateinit var suggestionItem: SuggestionItem

  @EpoxyAttribute(DoNotHash)
  lateinit var copyIcon: Drawable

  @EpoxyAttribute(DoNotHash)
  lateinit var historyIcon: Drawable

  @EpoxyAttribute(DoNotHash)
  lateinit var searchIcon: Drawable

  @EpoxyAttribute(DoNotHash)
  lateinit var onClickListener: View.OnClickListener

  @EpoxyAttribute(DoNotHash)
  lateinit var onLongClickListener: View.OnLongClickListener

  @EpoxyAttribute
  var query: String = ""

  override fun bind(holder: ViewHolder) {
    super.bind(holder)
    holder.apply {
      suggestionsText.text = suggestionItem.title.toSpannable().makeMatchingBold(query)
      when (suggestionItem.type) {
        COPY -> suggestionIcon.setImageDrawable(copyIcon)
        GOOGLE -> suggestionIcon.setImageDrawable(searchIcon)
        HISTORY -> suggestionIcon.setImageDrawable(historyIcon)
      }
      when {
        TextUtils.isEmpty(suggestionItem.subTitle) -> {
          suggestionsSubTitle.gone()
          suggestionsSubTitle.text = null
        }
        else -> {
          suggestionsSubTitle.show()
          suggestionsSubTitle.text = suggestionItem.subTitle
        }
      }
      containerView.setOnClickListener(onClickListener)
      containerView.setOnLongClickListener(onLongClickListener)
    }
  }

  class ViewHolder : KotlinHolder()
}