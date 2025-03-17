package com.example.my.project.authenticator.extensions

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.my.project.authenticator.R

fun Fragment.isAlive() = isAdded && !isDetached && !isRemoving && view != null

suspend fun Fragment?.isSuspendAlive(invoke: suspend () -> Unit) {
    val result = this != null && isAdded && !isDetached && !isRemoving && view != null
    if (result) {
        invoke()
    }
}

fun Activity.openFragment(
    destinationId: Int,
    addToBackStack: Boolean,
    bundle: Bundle? = null
) {

    val navController = Navigation
        .findNavController(
            findViewById(R.id.nav_host_fragment)
        )

    if (!addToBackStack) {
        navController.popBackStack()
    }

    /*val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.open_enter)
        .setExitAnim(R.anim.open_exit)
        .setPopEnterAnim(R.anim.close_enter)
        .setPopExitAnim(R.anim.close_exit)
        .build()*/

    navController.navigate(destinationId, bundle/*, navOptions*/)
}

/*fun Fragment.getPreviousFragmentId(): Int? {

    val navController = Navigation
        .findNavController(
            requireActivity()
                .findViewById(R.id.nav_host_fragment)
        )

    val previousDestinationId = navController.previousBackStackEntry?.destination?.id
    (requireActivity() as MainActivity).previousDestinationId = previousDestinationId

    return previousDestinationId
}*/

fun Fragment.popBackStack() {

    val navController = Navigation
        .findNavController(
            requireActivity()
                .findViewById(R.id.nav_host_fragment)
        )

    navController.popBackStack()
}

fun Fragment.openFragmentExclusively(currentFragmentId:Int,destinationId: Int, bundle: Bundle? = null) {

    val navController = Navigation.findNavController(
            requireActivity()
                .findViewById(R.id.nav_host_fragment)
        )

    /*val navOptions: NavOptions =
        NavOptions.Builder().setPopUpTo(currentFragmentId, true)
            .setEnterAnim(R.anim.open_enter)
            .setExitAnim(R.anim.open_exit)
            .setPopEnterAnim(R.anim.close_enter)
            .setPopExitAnim(R.anim.close_exit)
            .build()*/

    navController.navigate(destinationId, bundle/*, navOptions*/)
}