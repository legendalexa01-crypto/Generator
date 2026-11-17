package com.example.cryptogenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MENU_UUID = 1
        private const val MENU_HASH = 2
        private const val MENU_PASSWORD = 3

        // Ключи Bundle. Имена произвольные, важно — уникальность и совпадение put/get.
        private const val KEY_TAB = "state_tab"
        private const val KEY_UUID = "state_uuid"
        private const val KEY_HASH_INPUT = "state_hash_input"
        private const val KEY_HASH_ALGO = "state_hash_algo"
        private const val KEY_HASH_RESULT = "state_hash_result"
        private const val KEY_PASS_LEN = "state_pass_len"
        private const val KEY_PASS_RESULT = "state_pass_result"
    }

    private lateinit var contentContainer: FrameLayout
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var uuidResult: TextView
    private lateinit var hashInput: TextInputEditText
    private lateinit var hashAlgorithm: Spinner
    private lateinit var hashResult: TextView
    private lateinit var passLength: TextInputEditText
    private lateinit var passResult: TextView

    private val uuidScreen: View by lazy { buildUuidScreen() }
    private val hashScreen: View by lazy { buildHashScreen() }
    private val passwordScreen: View by lazy { buildPasswordScreen() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), 0)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.app_title)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(16))
        })

        contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        root.addView(contentContainer)

        bottomNav = BottomNavigationView(this)
        bottomNav.menu.add(0, MENU_UUID, 0, getString(R.string.tab_uuid))
        bottomNav.menu.add(0, MENU_HASH, 1, getString(R.string.tab_hash))
        bottomNav.menu.add(0, MENU_PASSWORD, 2, getString(R.string.tab_password))
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                MENU_UUID -> showScreen(uuidScreen)
                MENU_HASH -> showScreen(hashScreen)
                MENU_PASSWORD -> showScreen(passwordScreen)
            }
            true
        }
        root.addView(bottomNav)

        setContentView(root)

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = MENU_UUID
            // Явный вызов — на случай, если listener не сработал при первом выборе
            showScreen(uuidScreen)
        } else {
            restoreState(savedInstanceState)
        }
    }

    // ---------------- SAVE / RESTORE ----------------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, bottomNav.selectedItemId)
        // lateinit-поля инициализируются лениво, только при первом показе экрана.
        // Если пользователь не открывал вкладку — поля ещё не готовы, сохранять нечего.
        if (::uuidResult.isInitialized) outState.putString(KEY_UUID, uuidResult.text?.toString())
        if (::hashInput.isInitialized) outState.putString(KEY_HASH_INPUT, hashInput.text?.toString())
        if (::hashAlgorithm.isInitialized) outState.putInt(KEY_HASH_ALGO, hashAlgorithm.selectedItemPosition)
        if (::hashResult.isInitialized) outState.putString(KEY_HASH_RESULT, hashResult.text?.toString())
        if (::passLength.isInitialized) outState.putString(KEY_PASS_LEN, passLength.text?.toString())
        if (::passResult.isInitialized) outState.putString(KEY_PASS_RESULT, passResult.text?.toString())
    }

    private fun restoreState(state: Bundle) {
        // Форсируем построение всех экранов, чтобы все lateinit-поля были инициализированы
        // до того, как мы начнём в них что-то записывать.
        uuidScreen
        hashScreen
        passwordScreen

        state.getString(KEY_UUID)?.let { uuidResult.text = it }
        state.getString(KEY_HASH_INPUT)?.let { hashInput.setText(it) }
        hashAlgorithm.setSelection(state.getInt(KEY_HASH_ALGO, 0))
        state.getString(KEY_HASH_RESULT)?.let { hashResult.text = it }
        state.getString(KEY_PASS_LEN)?.let { passLength.setText(it) }
        state.getString(KEY_PASS_RESULT)?.let { passResult.text = it }

        val tab = state.getInt(KEY_TAB, MENU_UUID)
        bottomNav.selectedItemId = tab
        // Fallback: если selectedItemId уже был равен tab, listener мог не сработать,
        // и контейнер остался бы пустым.
        when (tab) {
            MENU_HASH -> showScreen(hashScreen)
            MENU_PASSWORD -> showScreen(passwordScreen)
            else -> showScreen(uuidScreen)
        }
    }

    // ---------------- ЭКРАН UUID ----------------

    private fun buildUuidScreen(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(16), dp(8), dp(16))
        }

        container.addView(TextView(this).apply {
            text = getString(R.string.uuid_hint)
            textSize = 16f
        })

        uuidResult = TextView(this).apply {
            text = getString(R.string.placeholder_press_generate)
            textSize = 16f
            setTextIsSelectable(true)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(20), dp(16), dp(20))
        }
        container.addView(resultCard(uuidResult))

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_generate)
            setOnClickListener { uuidResult.text = Generator.uuid() }
        })

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_copy)
            setOnClickListener { copyToClipboard(uuidResult.text.toString()) }
        })

        return wrapInScroll(container)
    }

    // ---------------- ЭКРАН ХЕША ----------------

    private fun buildHashScreen(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(16), dp(8), dp(16))
        }

        hashInput = TextInputEditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        container.addView(TextInputLayout(this).apply {
            hint = getString(R.string.hash_hint)
            addView(hashInput)
        })

        container.addView(TextView(this).apply {
            text = getString(R.string.hash_algorithm)
            setPadding(0, dp(16), 0, dp(4))
        })

        hashAlgorithm = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                HashAlgorithm.values().map { it.displayName }
            )
        }
        container.addView(hashAlgorithm)

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_compute_hash)
            setOnClickListener {
                val input = hashInput.text?.toString().orEmpty()
                if (input.isEmpty()) {
                    Toast.makeText(this@MainActivity, R.string.error_empty_input, Toast.LENGTH_SHORT).show()
                } else {
                    val algo = HashAlgorithm.values()[hashAlgorithm.selectedItemPosition]
                    hashResult.text = Generator.hash(input, algo)
                }
            }
        })

        hashResult = TextView(this).apply {
            text = getString(R.string.placeholder_hash)
            textSize = 13f
            setTextIsSelectable(true)
            setPadding(dp(16), dp(20), dp(16), dp(20))
        }
        container.addView(resultCard(hashResult))

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_copy)
            setOnClickListener { copyToClipboard(hashResult.text.toString()) }
        })

        return wrapInScroll(container)
    }

    // ---------------- ЭКРАН ПАРОЛЯ ----------------

    private fun buildPasswordScreen(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(16), dp(8), dp(16))
        }

        passLength = TextInputEditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("16")
        }
        container.addView(TextInputLayout(this).apply {
            hint = getString(R.string.password_length_hint)
            addView(passLength)
        })

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_generate)
            setOnClickListener {
                val length = passLength.text?.toString()?.toIntOrNull() ?: 0
                if (length !in 4..64) {
                    Toast.makeText(this@MainActivity, R.string.error_length_range, Toast.LENGTH_SHORT).show()
                } else {
                    passResult.text = Generator.password(length)
                }
            }
        })

        passResult = TextView(this).apply {
            text = getString(R.string.placeholder_password)
            textSize = 18f
            setTextIsSelectable(true)
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        container.addView(resultCard(passResult))

        container.addView(MaterialButton(this).apply {
            text = getString(R.string.btn_copy)
            setOnClickListener { copyToClipboard(passResult.text.toString()) }
        })

        return wrapInScroll(container)
    }

    // ---------------- ВСПОМОГАТЕЛЬНЫЕ ----------------

    private fun showScreen(view: View) {
        contentContainer.removeAllViews()
        contentContainer.addView(view)
    }

    private fun wrapInScroll(view: View): View =
        ScrollView(this).apply { addView(view) }

    private fun resultCard(inner: View): MaterialCardView =
        MaterialCardView(this).apply {
            addView(inner)
            setCardBackgroundColor(0xFFF2F2F2.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16); bottomMargin = dp(16) }
        }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("generated", text))
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}