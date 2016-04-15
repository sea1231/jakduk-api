package com.jakduk.service;

import com.jakduk.authentication.common.CommonPrincipal;
import com.jakduk.authentication.common.CommonUser;
import com.jakduk.authentication.common.OAuthPrincipal;
import com.jakduk.authentication.jakduk.JakdukPrincipal;
import com.jakduk.common.CommonConst;
import com.jakduk.common.CommonRole;
import com.jakduk.exception.DuplicateDataException;
import com.jakduk.exception.UnauthorizedAccessException;
import com.jakduk.model.db.FootballClub;
import com.jakduk.model.db.User;
import com.jakduk.model.embedded.CommonWriter;
import com.jakduk.model.embedded.LocalName;
import com.jakduk.model.simple.OAuthProfile;
import com.jakduk.model.simple.SocialUserOnAuthentication;
import com.jakduk.model.simple.UserOnPasswordUpdate;
import com.jakduk.model.simple.UserProfile;
import com.jakduk.model.web.*;
import com.jakduk.repository.FootballClubRepository;
import com.jakduk.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Slf4j
public class UserService {
	
	@Autowired
	private CommonService commonService;
	
	@Autowired
	private StandardPasswordEncoder encoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FootballClubRepository footballClubRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	public void create(User user) {
		StandardPasswordEncoder encoder = new StandardPasswordEncoder();
		
		user.setPassword(encoder.encode(user.getPassword()));
		userRepository.save(user);
	}
	
	public List<User> findAll() {
		return userRepository.findAll();
	}
	
	public CommonWriter testFindId(String userid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("email").is(userid));
		
		return mongoTemplate.findOne(query, CommonWriter.class);
	}

	public Model getUserWrite(Model model, String language) {

		List<FootballClub> footballClubs = commonService.getFootballClubs(language, CommonConst.CLUB_TYPE.FOOTBALL_CLUB, CommonConst.NAME_TYPE.fullName);

		model.addAttribute("userWrite", new UserWrite());
		model.addAttribute("footballClubs", footballClubs);
		
		return model;
	}
	
	public void saveUserOnSignUp(UserWrite userWrite) {
		
		User user = new User();
		user.setEmail(userWrite.getEmail().trim());
		user.setUsername(userWrite.getUsername().trim());
		user.setPassword(userWrite.getPassword().trim());
		user.setProviderId(CommonConst.ACCOUNT_TYPE.JAKDUK);
		
		String footballClub = userWrite.getFootballClub();
		String about = userWrite.getAbout();
		
		if (Objects.nonNull(footballClub) && !footballClub.isEmpty()) {
			FootballClub supportFC = footballClubRepository.findOne(userWrite.getFootballClub());
			
			user.setSupportFC(supportFC);
		}

		if (Objects.nonNull(about) && !about.isEmpty()) {
			user.setAbout(about.trim());
		}

		ArrayList<Integer> roles = new ArrayList<Integer>();
		roles.add(CommonRole.ROLE_NUMBER_USER_01);
		
		user.setRoles(roles);
		
		this.create(user);
		
		if (log.isDebugEnabled()) {
			log.debug("user=" + user);
		}
	}
	
	public void oauthUserWrite(SocialUserOnAuthentication oauthUserOnLogin) {
		User user = new User();
		
		user.setUsername(oauthUserOnLogin.getUsername());
		//user.setSocialInfo(oauthUserOnLogin.getSocialInfo());
		user.setRoles(oauthUserOnLogin.getRoles());
		
		userRepository.save(user);
	}
	
	public Boolean existEmail(Locale locale, String email) {
		Boolean result = false;

		if (commonService.isAnonymousUser()) {
			User user = userRepository.findOneByEmail(email);

			if (Objects.nonNull(user))
				throw new DuplicateDataException(commonService.getResourceBundleMessage(locale, "messages.user", "user.msg.already.email"));
		} else {
			throw new UnauthorizedAccessException(commonService.getResourceBundleMessage(locale, "messages.common", "common.exception.already.you.are.user"));
		}
		
		return result;
	}
	
	public Boolean existUsernameOnWrite(Locale locale, String username) {
		Boolean result = false;

		if (commonService.isAnonymousUser()) {
			User user = userRepository.findOneByUsername(username);

			if (Objects.nonNull(user))
				throw new DuplicateDataException(commonService.getResourceBundleMessage(locale, "messages.user", "user.msg.already.username"));
		} else {
			throw new UnauthorizedAccessException(commonService.getResourceBundleMessage(locale, "messages.common", "common.exception.already.you.are.user"));
		}

		return result;
	}
	
	public Boolean existUsernameOnUpdate(Locale locale, String username) {
		Boolean result = false;
		
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof JakdukPrincipal) {
			JakdukPrincipal principal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String id = principal.getId();
			
			UserProfile userProfile = userRepository.userFindByNEIdAndUsername(id, username);
			
			if (Objects.nonNull(userProfile))
				throw new DuplicateDataException(commonService.getResourceBundleMessage(locale, "messages.user", "user.msg.replicated.data"));
		} else {
			throw new UnauthorizedAccessException(commonService.getResourceBundleMessage(locale, "messages.common", "common.exception.access.denied"));
		}
		
		return result;
	}
	
	public Boolean existOAuthUsernameOnUpdate(String username) {
		Boolean result = false;
		
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OAuthPrincipal) {
			OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String oauthId = principal.getOauthId();
			
			OAuthProfile oauthProfile = userRepository.userFindByNEOauthIdAndUsername(oauthId, username);
			
			if (oauthProfile != null) result = true;
		} 
		
		return result;
	}
	
	public Model getOAuthWriteDetails(Model model, String language) {

		List<FootballClub> footballClubs = commonService.getFootballClubs(language, CommonConst.CLUB_TYPE.FOOTBALL_CLUB, CommonConst.NAME_TYPE.fullName);

		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OAuthPrincipal) {
			OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			CommonUser userDetails = (CommonUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
			
			SocialUserOnAuthentication oauthUserOnLogin = userRepository.findByOauthUser(principal.getProviderId(), principal.getOauthId());
			
			SocialUserForm oauthUserForm = new SocialUserForm();
			
			if (oauthUserOnLogin != null && oauthUserOnLogin.getUsername() != null) {
				oauthUserForm.setUsername(oauthUserOnLogin.getUsername());
			}
			
			if (userDetails != null && userDetails.getBio() != null) {
				oauthUserForm.setAbout(userDetails.getBio());
			}
			
			model.addAttribute("OAuthUserWrite", oauthUserForm);
		}

		model.addAttribute("footballClubs", footballClubs);
		
		return model;
	}
	
	public void checkOAuthProfileUpdate(SocialUserForm socialUserForm, BindingResult result) {
		
		OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String oauthId = principal.getOauthId();
		
		String username = socialUserForm.getUsername();
		
		if (oauthId != null && username != null) {
			OAuthProfile profile = userRepository.userFindByNEOauthIdAndUsername(oauthId, username);
			if (profile != null) {
				result.rejectValue("username", "user.msg.already.username");
			}
		}		
	}

	public void saveSocialUser(SocialUserForm userForm, CommonConst.ACCOUNT_TYPE providerId, String providerUserId) {

		User user = new User();

		user.setEmail(userForm.getEmail().trim());
		user.setUsername(userForm.getUsername().trim());
		user.setProviderId(providerId);
		user.setProviderUserId(providerUserId);


		ArrayList<Integer> roles = new ArrayList<Integer>();
		roles.add(CommonRole.ROLE_NUMBER_USER_01);

		user.setRoles(roles);

		String footballClub = userForm.getFootballClub();
		String about = userForm.getAbout();

		if (Objects.nonNull(footballClub) && !footballClub.isEmpty()) {
			FootballClub supportFC = footballClubRepository.findOne(footballClub);

			user.setSupportFC(supportFC);
		}

		if (Objects.nonNull(about) && !about.isEmpty()) {
			user.setAbout(userForm.getAbout().trim());
		}

		if (log.isDebugEnabled()) {
			log.debug("OAuth user=" + user);
		}

		userRepository.save(user);

//		principal.setUsername(userWrite.getUsername());
//		principal.setAddInfoStatus(socialInfo.getAddInfoStatus());
//
//		commonService.doOAuthAutoLogin(principal, credentials, userDetails);
	}
	
	public Model getUserProfile(Model model, String language, Integer status) {

		// OAuth 회원이 아닌, 작두왕 회원일 경우다. 그냥 이거는 테스트 용이고 나중에는 OAuth 전체 (페이스북, 다음)과 작두왕 회원에 대한 통합 Principal이 필요.
		JakdukPrincipal authUser = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserProfile user = userRepository.userProfileFindById(authUser.getId());
		
		UserProfileInfo userProfileInfo = new UserProfileInfo();
		userProfileInfo.setEmail(user.getEmail());
		userProfileInfo.setUsername(user.getUsername());
		userProfileInfo.setAbout(user.getAbout());
		
		FootballClub footballClub = user.getSupportFC();
		
		if (footballClub != null) {
			List<LocalName> names = footballClub.getNames();
			
			for (LocalName name : names) {
				if (name.getLanguage().equals(language)) {
					userProfileInfo.setFootballClubName(name);
				}		
			}
		}
				
		model.addAttribute("status", status);
		model.addAttribute("userProfile", userProfileInfo);
		
		return model;
	}
	
	public Model getUserProfileUpdate(Model model, String language) {

		if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

			List<FootballClub> footballClubs = commonService.getFootballClubs(language, CommonConst.CLUB_TYPE.FOOTBALL_CLUB, CommonConst.NAME_TYPE.fullName);
			
			JakdukPrincipal authUser = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			UserProfile userProfile = userRepository.userProfileFindById(authUser.getId());
			
			FootballClub footballClub = userProfile.getSupportFC();
			
			UserProfileWrite userProfileWrite = new UserProfileWrite();
			
			userProfileWrite.setEmail(userProfile.getEmail());
			userProfileWrite.setUsername(userProfile.getUsername());
			userProfileWrite.setAbout(userProfile.getAbout());

			if (footballClub != null) {
				userProfileWrite.setFootballClub(footballClub.getId());
			}
			
			model.addAttribute("userProfileWrite", userProfileWrite);
			model.addAttribute("footballClubs", footballClubs);
			
		} else {
		}
		
		return model;
	}
	
	public void checkProfileUpdate(UserProfileWrite userProfileWrite, BindingResult result) {
		
		JakdukPrincipal jakdukPrincipal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String id = jakdukPrincipal.getId();
		
		String username = userProfileWrite.getUsername();
		
		if (id != null && username != null) {
			UserProfile userProfle = userRepository.userFindByNEIdAndUsername(id, username);
			if (userProfle != null) {
				result.rejectValue("username", "user.msg.already.username");
			}
		}		
	}
	
	public void userProfileUpdate(UserProfileWrite userProfileWrite) {
		
		JakdukPrincipal principal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
		String id = principal.getId();
		
		User user = userRepository.findById(id);
		user.setUsername(userProfileWrite.getUsername());
		user.setAbout(userProfileWrite.getAbout());
		
		String footballClub = userProfileWrite.getFootballClub();
		
		if (footballClub != null && !footballClub.isEmpty()) {
			FootballClub supportFC = footballClubRepository.findOne(userProfileWrite.getFootballClub());
			
			user.setSupportFC(supportFC);
		}
		
		if (log.isInfoEnabled()) {
			log.info("jakduk user update. id=" + user.getId() + ", username=" + user.getUsername());
		}
		
		userRepository.save(user);
		
		principal.setUsername(userProfileWrite.getUsername());
		
		commonService.doJakdukAutoLogin(principal, credentials);
		
	}
	
	public Model getUserPasswordUpdate(Model model) {

		model.addAttribute("userPasswordUpdate", new UserPasswordUpdate());
		
		return model;
	}
	
	public void checkUserPasswordUpdate(UserPasswordUpdate userPasswordUpdate, BindingResult result) {
		
		String oldPwd = userPasswordUpdate.getOldPassword();
		String newPwd = userPasswordUpdate.getNewPassword();
		String newPwdCfm = userPasswordUpdate.getNewPasswordConfirm();
		
		JakdukPrincipal jakdukPrincipal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String id = jakdukPrincipal.getId();
		
		UserOnPasswordUpdate user = userRepository.userOnPasswordUpdateFindById(id);
		String pwd = user.getPassword();
		
		if (!encoder.matches(oldPwd, pwd)) {
			result.rejectValue("oldPassword", "user.msg.old.password.is.not.vaild");
		}
		
		if (!newPwd.equals(newPwdCfm)) {
			result.rejectValue("passwordConfirm", "user.msg.password.mismatch");
		}
	}
	
	public void userPasswordUpdate(UserPasswordUpdate userPasswordUpdate) {
		
		JakdukPrincipal jakdukPrincipal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String id = jakdukPrincipal.getId();
		
		User user = userRepository.findById(id);
		
		user.setPassword(userPasswordUpdate.getNewPassword());
		
		this.create(user);
		
		if (log.isInfoEnabled()) {
			log.info("jakduk user password was changed. id=" + user.getId() + ", username=" + user.getUsername());
		}
	}

	public void userPasswordUpdateByEmail(String email, String password) {
		User user = userRepository.findByEmail(email);
		user.setPassword(password);
		this.create(user);

		if (log.isInfoEnabled()) {
			log.info("jakduk user password was changed. id=" + user.getId() + ", username=" + user.getUsername());
		}
	}

	public Model getOAuthProfile(Model model, String language, Integer status) {

		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OAuthPrincipal) {
			OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			OAuthProfile profile = userRepository.userfindByOauthUser(principal.getProviderId(), principal.getOauthId());
			
			OAuthProfileInfo profileInfo = new OAuthProfileInfo();
			profileInfo.setUsername(profile.getUsername());
			profileInfo.setAbout(profile.getAbout());
			
			FootballClub footballClub = profile.getSupportFC();

			if (footballClub != null) {
				List<LocalName> names = footballClub.getNames();
				
				for (LocalName name : names) {
					if (name.getLanguage().equals(language)) {
						profileInfo.setFootballClubName(name);
					}		
				}
			}
					
			model.addAttribute("oauthProfile", profileInfo);
		}
		
		model.addAttribute("status", status);
		
		return model;
	}
	
	public Model getOAuthProfileUpdate(Model model, String language) {
		
		if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

			List<FootballClub> footballClubs = commonService.getFootballClubs(language, CommonConst.CLUB_TYPE.FOOTBALL_CLUB, CommonConst.NAME_TYPE.fullName);
			
			if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OAuthPrincipal) {
				OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				OAuthProfile profile = userRepository.userfindByOauthUser(principal.getProviderId(), principal.getOauthId());
				
				FootballClub footballClub = profile.getSupportFC();
				
				SocialUserForm socialUserForm = new SocialUserForm();
				
				socialUserForm.setUsername(profile.getUsername());
				socialUserForm.setAbout(profile.getAbout());

				if (footballClub != null) {
					socialUserForm.setFootballClub(footballClub.getId());
				}
				
				model.addAttribute("OAuthUserWrite", socialUserForm);
			}
			
			model.addAttribute("footballClubs", footballClubs);
			
		} else {
		}
		
		return model;
	}
	
	public void oAuthProfileUpdate(SocialUserForm userWrite) {
		
		if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
			CommonUser userDetails = (CommonUser) SecurityContextHolder.getContext().getAuthentication().getDetails();
			
			User user = userRepository.userFindByOauthUser(principal.getProviderId(), principal.getOauthId());

			String username = userWrite.getUsername();
			String footballClub = userWrite.getFootballClub();
			String about = userWrite.getAbout();
			
			if (username != null && !username.isEmpty()) {
				user.setUsername(userWrite.getUsername());
			}
			
			if (footballClub != null && !footballClub.isEmpty()) {
				FootballClub supportFC = footballClubRepository.findOne(userWrite.getFootballClub());
				
				user.setSupportFC(supportFC);
			}
			
			if (about != null && !about.isEmpty()) {
				user.setAbout(userWrite.getAbout());
			}

			userRepository.save(user);
			
			principal.setUsername(userWrite.getUsername());
			
			commonService.doOAuthAutoLogin(principal, credentials, userDetails);
		}
	}
	
	public CommonPrincipal getCommonPrincipal() {
		CommonPrincipal commonPrincipal = new CommonPrincipal();
		
		if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
			
			if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof OAuthPrincipal) {
				OAuthPrincipal principal = (OAuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				commonPrincipal.setId(principal.getId());
				commonPrincipal.setUsername(principal.getUsername());
				commonPrincipal.setType(principal.getProviderId());
			} else if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof JakdukPrincipal) {
				JakdukPrincipal principal = (JakdukPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				commonPrincipal.setId(principal.getId());
				commonPrincipal.setUsername(principal.getUsername());
				commonPrincipal.setType(principal.getProviderId());
			}
		}
		
		return commonPrincipal;
	}
		
}
