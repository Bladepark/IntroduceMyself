package com.example.introducemyself

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class SignUpActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_ENTRY_TYPE = "extra_entry_type"
        private const val EXTRA_MEMBER_DATA = "extra_member_data"
        fun newIntent(context: Context, entryType: SignUpEntryType, memberData: MemberData): Intent = Intent(
            context,
            SignUpActivity::class.java
        ).apply {
            putExtra(EXTRA_ENTRY_TYPE, entryType.ordinal)
            putExtra(EXTRA_MEMBER_DATA , memberData)
        }
    }

    private val entryType : SignUpEntryType by lazy {
        SignUpEntryType.getEntryType(
            intent?.getIntExtra(
                EXTRA_ENTRY_TYPE,
                0
            )
        )
    }

    private val memberData: MemberData? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                EXTRA_MEMBER_DATA, MemberData::class.java
            )
        } else {
            intent?.getParcelableExtra(EXTRA_MEMBER_DATA)
        }
    }

    private val nameEditText by lazy { findViewById<EditText>(R.id.nameTextInputEditText) }
    private val ageEditText by lazy { findViewById<EditText>(R.id.ageTextInputEditText) }
    private val mbtiEditText by lazy { findViewById<EditText>(R.id.mbtiTextInputEditText) }
    private val idEditText by lazy { findViewById<EditText>(R.id.idTextInputEditText) }
    private val pwdEditText by lazy { findViewById<EditText>(R.id.pwdTextInputEditText) }
    private val pwdCheckEditText by lazy { findViewById<EditText>(R.id.pwdCheckTextInputEditText) }
    private val emailEditText by lazy { findViewById<EditText>(R.id.emailTextInputEditText) }
    private val emailTextInputLayout by lazy { findViewById<View>(R.id.emailTextInputLayout) }
    private val signUpBtn by lazy { findViewById<Button>(R.id.btn_signUp) }
    private val spinner by lazy { findViewById<Spinner>(R.id.spinner) }


    private val editTexts
        get() = listOf(
            nameEditText,
            ageEditText,
            mbtiEditText,
            idEditText,
            emailEditText,
            pwdEditText,
            pwdCheckEditText
        )

    private val viewModel by lazy {
        ViewModelProvider(this)[SignUpViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initView()
        initViewModel()
    }

    private fun initViewModel() = with(viewModel) {
        viewModel.nameErrMsg.observe(this@SignUpActivity, Observer {
            nameEditText.error = it
        })

        viewModel.ageErrMsg.observe(this@SignUpActivity, Observer {
            ageEditText.error = it
        })

        viewModel.mbtiErrMsg.observe(this@SignUpActivity, Observer {
            mbtiEditText.error = it
        })

        viewModel.idErrMsg.observe(this@SignUpActivity, Observer {
            idEditText.error = it
        })

        viewModel.pwdErrMsg.observe(this@SignUpActivity, Observer {
            pwdEditText.error = it
        })

        viewModel.pwdCheckErrMsg.observe(this@SignUpActivity, Observer {
            pwdCheckEditText.error = it
        })

        viewModel.emailErrMsg.observe(this@SignUpActivity, Observer {
            emailEditText.error = it
        })

        viewModel.isSignUpBtnEnabled.observe(this@SignUpActivity, Observer {
            signUpBtn.isEnabled = it
        })
    }

    private fun initView() {
        setServiceProvider()

        setTextChangedListener()

        setMemberData()

        setSignUpButton()
    }

    private fun setMemberData() {
        if(entryType == SignUpEntryType.CREAT) {
            return
        }
        val emailProvider = memberData?.id?.split('@')
        Log.d("SignUpActivity", "Email Provider: $emailProvider")
        nameEditText.setText(memberData?.name)
        ageEditText.setText(memberData?.age)
        mbtiEditText.setText(memberData?.mbti)
        idEditText.setText(emailProvider?.getOrNull(0))
        pwdEditText.setText(memberData?.pwd)

        signUpBtn.text = getString(R.string.sign_up_edit_profile_finish)


        when(emailProvider?.getOrNull(1)) {
            "gmail.com" -> {
                spinner.setSelection(2)
            }
            "naver.com" -> {
                spinner.setSelection(3)
            }
            "kakao.com" -> {
                spinner.setSelection(4)
            }
            else -> spinner.setSelection(1)
        }
    }

    private fun setTextChangedListener() {
        editTexts.forEach { editText ->
            editText.addTextChangedListener {
                when(editText) {
                    nameEditText -> viewModel.getMessageValidName(nameEditText.text.toString())
                    ageEditText -> viewModel.(ageEditText.text.toString())
                    mbtiEditText -> viewModel.setErrorMessage(mbtiEditText.text.toString())
                    idEditText -> viewModel.setErrorMessage(idEditText.text.toString())
                    pwdEditText -> viewModel.setErrorMessage(pwdEditText.text.toString())
                    pwdCheckEditText -> viewModel.setErrorMessage(pwdCheckEditText.text.toString())
                    emailEditText -> viewModel.setErrorMessage(emailEditText.text.toString())
                }
                setSignUpButtonEnabled()
            }
        }
    }

    private fun setSignUpButton() {
        signUpBtn.setOnClickListener {
            val inputId = when (spinner.selectedItemPosition) {
                1 -> idEditText.text.toString() + emailEditText.text.toString()
                2, 3, 4 -> idEditText.text.toString() + spinner.selectedItem.toString()
                else -> idEditText.text.toString()
            }
            val inputPwd = pwdEditText.text.toString()
            if (!inputId.matches(Regex("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}\$"))) {
                Toast.makeText(this, getString(R.string.sign_up_id_error_pattern), Toast.LENGTH_SHORT).show()
            } else {
                if (entryType == SignUpEntryType.UPDATE) {
                    val updatedMember = MemberData(
                        id = inputId,
                        pwd = inputPwd,
                        name = nameEditText.text.toString(),
                        age = ageEditText.text.toString(),
                        mbti = mbtiEditText.text.toString()
                    )
                    MemberInfo.updateMember(memberData?.id, updatedMember)
                    Toast.makeText(this, "프로필 수정 완료!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("memberId", inputId)
                    intent.putExtra("memberPwd", inputPwd)
                    startActivity(intent)
                } else {
                    val newMember = MemberData(
                        id = inputId,
                        pwd = inputPwd,
                        name = nameEditText.text.toString(),
                        age = ageEditText.text.toString(),
                        mbti = mbtiEditText.text.toString()
                    )

                    MemberInfo.memberInfo.add(newMember)
                    Toast.makeText(this, "가입 완료! 로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.putExtra("memberId", inputId)
                    intent.putExtra("memberPwd", inputPwd)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun setServiceProvider() {
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.sign_up_email_provider_select_menu),
                getString(R.string.sign_up_email_provider_direct),
                getString(R.string.sign_up_email_provider_gmail),
                getString(R.string.sign_up_email_provider_naver),
                getString(R.string.sign_up_email_provider_kakao)
            )
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> signUpBtn.isEnabled = false
                    1 -> {
                        spinner.visibility = View.GONE
                        emailTextInputLayout.visibility = View.VISIBLE
                    }

                    else -> signUpBtn.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setSignUpButtonEnabled() {
        signUpBtn.isEnabled = when (spinner.selectedItemPosition) {
            0 -> false
            1 -> {
                isValidName()?.isBlank() ?: true
                        && isValidAge()?.isBlank() ?: true
                        && isValidMbti()?.isBlank() ?: true
                        && isValidId()?.isBlank() ?: true
                        && isValidPwd()?.isBlank() ?: true
                        && isValidPwdCheck()?.isBlank() ?: true
                        && isValidEmail()?.isBlank() ?: true
            }

            2, 3, 4 -> {
                isValidName()?.isBlank() ?: true
                        && isValidAge()?.isBlank() ?: true
                        && isValidMbti()?.isBlank() ?: true
                        && isValidId()?.isBlank() ?: true
                        && isValidPwd()?.isBlank() ?: true
                        && isValidPwdCheck()?.isBlank() ?: true
            }
            else -> false
        }
    }

}